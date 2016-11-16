


	import java.io.IOException;
	import java.net.InetAddress;
	import java.net.URL;
	import java.nio.file.DirectoryNotEmptyException;
	import java.nio.file.Files;
	import java.nio.file.LinkOption;
	import java.nio.file.Path;
	import java.nio.file.Paths;
	import java.nio.file.attribute.FileAttribute;
	import java.nio.file.attribute.PosixFilePermission;
	import java.nio.file.attribute.PosixFilePermissions;
	import java.util.Comparator;
	import java.util.Map.Entry;
	import java.util.PriorityQueue;
	import java.util.Set;
	import java.util.UUID;
	import java.util.concurrent.ConcurrentHashMap;

	import javax.annotation.PostConstruct;

	import org.slf4j.Logger;
	import org.slf4j.LoggerFactory;
	import org.springframework.beans.factory.annotation.Autowired;
	import org.springframework.scheduling.annotation.Scheduled;
	import org.springframework.stereotype.Component;

	import com.amazonaws.Protocol;
	
	import static com.google.common.base.Preconditions.checkArgument;

	@Component("InMemorySystem")
	public class VirtualSystem {

	    private static final Logger gLogger = LoggerFactory.getLogger(VirtualStorageAllocationSystem.class);

	    @Autowired
	    StorageSystemLinkage mStorageSystemLinkage;

	    @Autowired
	    StorageAgentConfig mStorageAgentConfig;

	    // Stores the Media Allocation Object:CdvrStorageModuleStorageAllocate based
	    // on Upload Name of the media.
	    // Key is Upload Name
	    private ConcurrentHashMap<String, StorageAllocateWrapper> mByUploadNameMediaObject = new ConcurrentHashMap<>();

	    // Stores the Media Allocation Object:CdvrStorageModuleStorageAllocate based
	    // on Storage Path of the media.
	    // Key is Storage Path
	    private ConcurrentHashMap<String, StorageAllocateWrapper> mByStoragePathMediaObject = new ConcurrentHashMap<>();

	    @Autowired
	    ISystemClock mClock;

	    // This queue is used to store the sorted end times of allocations in virtual memory. The end time is calculated
	    // based
	    // on the end time of CdvrStorageModuleStorageAllocate and little additional time, based on dynvar config. This
	    // additional time
	    // is added to give enough time for transcoder to store media files in storage.
	    PriorityQueue<Long> mQueueAllocationEndTime = new PriorityQueue<Long>();

	    PriorityQueue<CdvrStorageModuleStorageAllocate> endTimeQueue = new PriorityQueue<CdvrStorageModuleStorageAllocate>(
	            1000, new Comparator<CdvrStorageModuleStorageAllocate>() {
	                public int compare(CdvrStorageModuleStorageAllocate moduleStorageAllocate,
	                        CdvrStorageModuleStorageAllocate storageAllocate) {
	                    return (int) (getEndTime(moduleStorageAllocate) - getEndTime(moduleStorageAllocate));
	                }
	            });

	    private URL mMediaTranscoderUrl = null;

	    private Protocol mProtocol = Protocol.HTTP;

	    private int mPort;

	    private InetAddress mHostname = null;

	    @PostConstruct
	    public void init() throws Exception {
	        mProtocol = Protocol.HTTP;
	        mPort = mStorageAgentConfig.getStorageNodePort();
	        mHostname = InetAddress.getLocalHost();
	        mMediaTranscoderUrl = new URL(mProtocol.toString(), mHostname.getHostName(), mPort,
	                mStorageAgentConfig.getMediaNgnixDirectoryPathname());
	    }

	    @Override
	    public CdvrStorageModuleStorageAllocate lookupByStoragePath(URL url) {
	        StorageAllocateWrapper allocateWrapper = mByStoragePathMediaObject.get(url.toString());
	        if (allocateWrapper == null)
	            return null;

	        return allocateWrapper.getModuleStorageAllocate();

	    }

	    /**
	     * <p>
	     * Once media allocation is done, it returns storagePath. Cancellation is done made on storagePath. It will trigger
	     * following events.
	     * </p>
	     * <ul>
	     * <li>Remove allocation from Hashmap: mByStoragePathMediaObject based on storagePath.</li>
	     * <li>Remove allocation from Hashmap: mByUploadNameMediaObject based on uploadName.</li>
	     * <li>Remove allocation from PriorityQueue: queueAllocationEndTime based on endTime. storagePath.</li>
	     * </ul>
	     */
	    @Override
	    public Success cancelByStoragePath(URL url) {
	        CdvrStorageModuleStorageAllocate storageAllocateObject = (mByStoragePathMediaObject.get(url.toString())).getModuleStorageAllocate();
	        checkArgument(storageAllocateObject != null);
	        mByStoragePathMediaObject.remove(url.toString());
	        mByUploadNameMediaObject.remove(storageAllocateObject.getUploadName().toString());
	        mQueueAllocationEndTime.remove(getEndTime(storageAllocateObject));
	        String folderPath = url.getPath();
	        Path directoryPath = Paths.get(folderPath);
	        gLogger.info("Delete allocated folder from disk {}", folderPath);
	        if (Files.exists(directoryPath, new LinkOption[] {LinkOption.NOFOLLOW_LINKS})) {
	            try {
	                // This will delete the whole sub-directory
	                Files.delete(directoryPath);
	            } catch (IOException e) {
	                // TODO Auto-generated catch block
	                e.printStackTrace();
	            }

	        }
	        gLogger.info("Cancel allocated space for media upload named {}", directoryPath.toAbsolutePath());
	        return new Success();
	    }

	    @Override
	    public CdvrStorageModuleStorageAllocate lookupByUploadName(String uploadName) {
	        StorageAllocateWrapper allocateWrapper = mByUploadNameMediaObject.get(uploadName);
	        if (allocateWrapper == null)
	            return null;

	        return allocateWrapper.getModuleStorageAllocate();

	    }

	    /**
	     * Allocates storage for Media Object of Calling API. It adds object in HashMap: mByUploadNameMediaObject, if its
	     * successful then it creates StoragePath for the object and it adds object in : mByStoragePathMediaObject. End time
	     * of media allocation object is calculated and this end time is added to heap.
	     * 
	     * @throws IOException
	     */
	    @Override
	    public CdvrStorageModuleStorage allocateStorage(CdvrStorageModuleStorageAllocate moduleStorageAllocate)
	            throws SMAException, TrioException, IOException {
	        // check if Allocation is already done, if yes, throw exceptions
	        gLogger.debug("Allocating Storage in Virtual storage system, upload name {}",
	                moduleStorageAllocate.getUploadName());
	        if (mByUploadNameMediaObject.contains(moduleStorageAllocate.getUploadName())) {
	            String msg = String.format("Existing Mapping for this upload name {}",
	                    moduleStorageAllocate.getUploadName());
	            gLogger.error(msg);
	            throw new SMAException(msg);
	        }
	        long freeSpaceOnDisk = mStorageSystemLinkage.getMediaStorageUsableSpaceInKB();
	        long currentAllocatedStorage = getAllocatedStorage();
	        long mediaAllocationNeeded = moduleStorageAllocate.getSpaceNeededKB();
	        URL storagePath = null;
	        // Check that no. of allocations in memory shouldn't be more than
	        // maximum no. of uploads
	        // allowed in variable.
	        // TODO this checks for all allocations but should be refined in future
	        // to check for actual
	        // overlapping allocations - eg. if one allocation ends and then another
	        // starts, that's just
	        // one concurrent allocation, not two.
	        removeCompletedAllocationsFromMemory();
	        if (mQueueAllocationEndTime.size() >= mStorageAgentConfig.getMaxConcurrentUploads()) {
	            Error error = new Error();
	            error.setCode(ErrorCode.INTERNALERROR);
	            error.setText("No. of allocations have reached upper limit");
	            gLogger.error("No more allocations as no. of allocations have reached upper limit, "
	                    + "current limit is {} and current concurrent allocations are " + "{}",
	                    mStorageAgentConfig.getMaxConcurrentUploads(), mQueueAllocationEndTime.size());
	            throw new TrioException(error);
	        }

	        if ((freeSpaceOnDisk - currentAllocatedStorage) <= mediaAllocationNeeded) {
	            Error error = new Error();
	            error.setCode(ErrorCode.INTERNALERROR);
	            error.setText("Not enough storage On this node");
	            gLogger.error(
	                    "Not enough space on disk for media allocation, current free space is {}, current allocated space"
	                            + "is {}, space required for media {} is {}", freeSpaceOnDisk, currentAllocatedStorage,
	                    moduleStorageAllocate.getUploadName(), mediaAllocationNeeded);
	            throw new TrioException(error);
	        } else {
	            synchronized (moduleStorageAllocate.getUploadName().intern()) {
	                StorageAllocateWrapper allocationDS = new StorageAllocateWrapper();
	                storagePath = getStoragePathURL(moduleStorageAllocate.getUploadName());
	                allocationDS.setModuleStorageAllocate(moduleStorageAllocate);
	                allocationDS.setStoragePathRoot(storagePath);
	                mByUploadNameMediaObject.put(moduleStorageAllocate.getUploadName(), allocationDS);

	                mByStoragePathMediaObject.put(storagePath.toString(), allocationDS);
	                mQueueAllocationEndTime.add(getEndTime(moduleStorageAllocate));
	                gLogger.info("Allocated space for media upload named {}", moduleStorageAllocate.getUploadName());
	            }
	        }
	        // Create the response and send it back after allocation
	        CdvrStorageModuleStorage cdvrStorageModuleStorage = new CdvrStorageModuleStorage();
	        cdvrStorageModuleStorage.setStoragePathRoot(storagePath);
	        cdvrStorageModuleStorage.setCanNodeStore(true);
	        cdvrStorageModuleStorage.setMediaPathRoot(storagePath);

	        return cdvrStorageModuleStorage;
	    }

	    /**
	     * This method does creation of folder for media files to be stored. It takes two dynvars to get the directory path
	     * for creation of media files. Other dynvar value to be included in URL of storagepath sent to transcoder. The
	     * reason to have two dynvars for the direcotry path is we may have another folder for media directory creation or
	     * we might change name of that. But while sending Storage URL to Nginx, we are adding 'upload' in path for POST
	     * request, and removing the absolute path from URL. ie. Actual URL for files in disk:
	     * /TivoData/containers/data/mediastreamer/media1/9ace5c65-8272-470c/ ex: Storage URL for ngnix
	     * http://ip:port/upload/media1/9ace5c65-8272-470c/ here dynvar 1 is :/TivoData/containers/data/mediastreamer/
	     * Dynvar 2: /media1
	     * 
	     * @param uploadName
	     * @return
	     * @throws SMAException
	     */
	    private URL getStoragePathURL(String uploadName) throws SMAException {
	        String storageName = UUID.randomUUID().toString() + "_" + uploadName;
	        Path directoryPath = null;
	        URL url = null;
	        try {
	            directoryPath = Paths.get(mStorageAgentConfig.getMediaDiskRootDirectoryPathname(), storageName);
	            Set<PosixFilePermission> perms = PosixFilePermissions.fromString("rwxrwxrwx");
	            FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(perms);
	            if (!Files.exists(directoryPath, new LinkOption[] {LinkOption.NOFOLLOW_LINKS})) {
	                Path dirPath = Files.createDirectories(directoryPath, attr);
	                Files.setPosixFilePermissions(dirPath, perms);

	            }
	            gLogger.debug("Directory Path for media {} is {}", uploadName, directoryPath);

	            if (!Files.exists(directoryPath, new LinkOption[] {LinkOption.NOFOLLOW_LINKS})) {
	                String msg = String.format("Not able to create directory {} for upload {}", directoryPath, uploadName);
	                gLogger.error(msg);
	                throw new SMAException(msg);
	            }
	            url = new URL(mMediaTranscoderUrl.getProtocol(), mMediaTranscoderUrl.getHost(),
	                    mMediaTranscoderUrl.getPort(), mMediaTranscoderUrl.getPath() + "/" + storageName);
	        } catch (IOException e) {
	            gLogger.error("Error creating storage Path for allocation");
	            e.printStackTrace();

	        }
	        return url;
	    }

	    /**
	     * Get total storage Allocated in Memory. This method is gets info of storage needed from each
	     * CdvrStorageModuleStorageAllocate in memory and returns total storage allocated in the virtual memory at given
	     * point.
	     */
	    @Override
	    public long getAllocatedStorage() {
	        long currentAllocatedSize = 0;
	        for (StorageAllocateWrapper moduleStorageAllocate : mByStoragePathMediaObject.values()) {
	            currentAllocatedSize += moduleStorageAllocate.getModuleStorageAllocate().getSpaceNeededKB();
	        }
	        return currentAllocatedSize;
	    }

	    private Long getEndTime(CdvrStorageModuleStorageAllocate moduleStorageAllocate) {
	        Integer duration = moduleStorageAllocate.getDuration();
	        long allocationEndtime = moduleStorageAllocate.getStartDate().getTime()

	        + duration.longValue() + mStorageAgentConfig.getPeriodPostrecord();
	        return Long.valueOf(allocationEndtime);
	    }

	    // Delay in milliseconds
	    @Scheduled(fixedDelay = 300000)
	    public void removeCompletedAllocationsFromMemory() {
	        gLogger.debug("Starting task to remove stale allocations from memory ");
	        if (mQueueAllocationEndTime.size() == 0) {
	            gLogger.debug("Currently no allocations in memory, mQueueAllocationEndTime is empty.");
	            return;
	        }
	        long endTimeForMediaAllocation = mQueueAllocationEndTime.peek();
	        gLogger.info("Size of allocation cache Object mByStoragePathMediaObject {}, mByUploadNameMediaObject {} ",
	                mByStoragePathMediaObject.size(), mByUploadNameMediaObject.size());
	        if (mClock.dateNow().getTime() > endTimeForMediaAllocation) {
	            for (Entry<String, StorageAllocateWrapper> moduleStorageAllocateEntry : mByUploadNameMediaObject.entrySet()) {
	                String storagePath = null;
	                String uploadName = null;
	                long endTimeForIndividualMedia = getEndTime(moduleStorageAllocateEntry.getValue().getModuleStorageAllocate());
	                if (endTimeForIndividualMedia < mClock.dateNow().getTime()) {
	                    uploadName = moduleStorageAllocateEntry.getValue().getModuleStorageAllocate().getUploadName();
	                    storagePath = moduleStorageAllocateEntry.getValue().getStoragePathRoot().toString();
	                    // Delete these entries from Hashmaps
	                    mByStoragePathMediaObject.remove(storagePath);
	                    mByUploadNameMediaObject.remove(uploadName);
	                    mQueueAllocationEndTime.poll();
	                    gLogger.info("Deleted stale value of upload {} with endtime {}", uploadName,
	                            endTimeForMediaAllocation);
	                    try {
	                        // Now delete empty folders. Empty folders implicate no recording was stored in them
	                        String folderPath = (new URL(storagePath)).getPath();
	                        Path directoryPath = Paths.get(folderPath);
	                        // This will delete the whole sub-directory
	                        // This will throw exception and will not delete
	                        // directory incase directory is not empty. Which is
	                        // what we not ie not to delete, directory with
	                        // data.
	                        Files.deleteIfExists(directoryPath);
	                        gLogger.info("Delete allocated folder from disk {}", storagePath);
	                    } catch (DirectoryNotEmptyException e) {
	                        gLogger.error("Directory will not be deleted as it had data, folderPath {}", storagePath);
	                    } catch (IOException e) {
	                        gLogger.error("I/O exception while trying to clear data {}", storagePath);
	                    }
	                }
	            }
	            gLogger.info(
	                    "Size of allocation cache Object after removing stale values mByStoragePathMediaObject {}, mByUploadNameMediaObject {} ",
	                    mByStoragePathMediaObject.size(), mByUploadNameMediaObject.size());

	        }
	    }

	}

	&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
	&&&&&&&
	@RequestMapping(value = "/cdvrStorageModuleMediaDelete", method = RequestMethod.POST, produces = {
				"application/json", "application/xml" })
		public Success cdvrStorageModuleMediaDelete(
				@RequestBody CdvrStorageModuleMediaDelete CdvrStorageModuleDelete)
						throws SMAException {
			Error error = new Error();
			checkArgument(CdvrStorageModuleDelete != null);
			checkArgument(CdvrStorageModuleDelete.getMediaPath() != null);

			if (!mStorageSystemLinkage
					.deleteMediaFile(filePath(CdvrStorageModuleDelete
							.getMediaPath()))) {
				error.setCode(ErrorCode.INTERNALERROR);
				error.setText("Media file not found at this path");
				TrioException ex = new TrioException(error);
				throw ex;
			}

			return new Success();
		}



	&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&

	ublic class StorageAgentIntTest extends StorageAgentBaseTest {
		private static final Logger gLogger = LoggerFactory
				.getLogger(StorageAgentIntTest.class);

		@Autowired
		StorageSystemLinkage mStorageSystemLinkage;
		@Autowired
		StorageAgentClient mStorageAgentClient;
		@Autowired
		VirtualStorageAllocationSystem mAllocationSystem;
		@Autowired
		StorageAgentConfig mStorageAgentConfig;

		final static Path mRootPath = Paths.get("");
		final String mFilenameExtension = "mp4";
		private static boolean isInitialized = false;

		private long mAvailableStorageForAllocation = 0;
		private long storageMediaSize = 0;

		public StorageAgentIntTest() {
			init();
		}

		private static void init() {
			if (!isInitialized) {
				Properties properties = new Properties();
				properties.setProperty(
						SmaConstants.MEDIA_DISK_ROOT_DIRECTORY_PROPNAME, mRootPath
								.toFile().getAbsolutePath());
		        properties.setProperty(SmaConstants.MEDIA_NGNIX_DIRECTORY, "/upload/media1");                

				ConfigurationManager.loadProperties(properties);
				isInitialized = true;
			}
		}

		@Test
		public void checkReadiness() throws URISyntaxException {
			// Verify that service is up.
			verifyReady();
			assertTrue(true);
		}

		@Test
		public void testStorageServiceFileApiSimple() throws RestClientException,
				URISyntaxException {
			gLogger.info("Starting tests for Storage Module Agent module.");

			Set<PosixFilePermission> perms = PosixFilePermissions
					.fromString("rw-rw----");
			FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions
					.asFileAttribute(perms);

			String fileName = String.format("%s.%s",
					RandomStringUtils.randomAlphanumeric(16), mFilenameExtension);

			Path filePath = Paths.get(
					mStorageAgentConfig.getMediaDiskRootDirectoryPathname(),
					fileName);
					///mRootPath.resolve(mStorageAgentConfig.getMediaDiskRootDirectoryPathname()).resolve(mStorageAgentConfig.getMediaDirectory()).resolve(fileName);
			try {
				Files.createFile(filePath, attr);
			} catch (IOException e) {
				gLogger.error("Failed to create file ={}", filePath);
			}

			ensureFileValid(fileName);

			// cleanup, no need to check for status since this is a simple test
			testDeletion(fileName);
		}

		@Test
		public void testStorageServiceFileApiFlow() throws RestClientException,
				URISyntaxException {
			gLogger.info("Starting tests for Storage Module Agent module.");

			Set<PosixFilePermission> perms = PosixFilePermissions
					.fromString("rw-r-----");
			FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions
					.asFileAttribute(perms);

			String fileName = String.format("%s.%s",
					RandomStringUtils.randomAlphanumeric(16), mFilenameExtension);

			Path filePath = Paths.get(
					mStorageAgentConfig.getMediaDiskRootDirectoryPathname(),
					fileName);

			try {
				Files.createFile(filePath, attr);
			} catch (IOException e) {
				gLogger.error("Failed to create file ={}", filePath);
			}

			ensureFileValid(fileName);

			ensureDeletion(fileName);

			ensureFileInvalid(fileName);

			ensureDeletionFails(fileName);
		}

		@Test
		public void testStorageServiceDiskSpaceAPISimple()
				throws RestClientException, URISyntaxException {
			gLogger.info("Starting tests for Storage Module Agent module.");

			long usableSpace = mStorageSystemLinkage
					.getMediaStorageUsableSpaceInKB();

			long usedSpace = mStorageSystemLinkage.getMediaStorageUsedSpaceInKB();

			long totalSpace = mStorageSystemLinkage
					.getMediaStorageTotalDiskSpaceInKB();

			assertTrue((usableSpace + usedSpace) <= totalSpace);
		}

		@Test
		public void testStorageServiceMemoryApiSimple() throws RestClientException,
				URISyntaxException {
			gLogger.info("Starting tests for Storage Module Agent module.");

			long freeMem = mStorageSystemLinkage.getFreeMemoryInKB();

			long usedMem = mStorageSystemLinkage.getUsedMemoryInKB();

			long totalMem = mStorageSystemLinkage.getTotalMemoryInKB();

			assertTrue((freeMem + usedMem) <= totalMem);
		}

		@Test
		public void testStorageServiceCpuApiSimple() throws RestClientException,
				URISyntaxException {
			gLogger.info("Starting tests for Storage Module Agent module.");

			double cpuPercentage = mStorageSystemLinkage.getCPUUsagePercentage();
			assertTrue(cpuPercentage >= 0 && cpuPercentage <= 100);
		}

		private boolean ensureDeletionFails(String fileName) {
			boolean status = testDeletion(fileName);
			assertFalse(status);
			return status;
		}

		private boolean ensureDeletion(String fileName) {
			boolean status = testDeletion(fileName);
			assertTrue(status);
			return status;
		}

		private boolean testDeletion(String fileName) {
			boolean status = false;
			try {
				status = mStorageSystemLinkage.deleteMediaFile(fileName);
			} catch (SMAException e) {
				e.printStackTrace();
			}
			return status;
		}

		private boolean ensureFileInvalid(String fileName) {
			boolean status = testFileValidity(fileName);
			assertFalse(status);
			return status;
		}

		private boolean ensureFileValid(String fileName) {
			boolean status = testFileValidity(fileName);
			assertTrue(status);
			return status;
		}

		private boolean testFileValidity(String fileName) {
			boolean status = false;
			try {
				status = mStorageSystemLinkage.isMediaFileValid(fileName);
			} catch (SMAException e) {
				e.printStackTrace();
			}
			return status;
		}

		/**
		 * This API tests all APIs of StorageAgentService class, for invalid values.
		 * 
		 * @throws Exception
		 */
		@Test
		public void testStorageAgentServiceApi_InvalidValues() throws Exception {
			Set<PosixFilePermission> perms = PosixFilePermissions
					.fromString("rw-r-----");
			FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions
					.asFileAttribute(perms);

			String fileName = String.format("%s.%s",
					RandomStringUtils.randomAlphanumeric(16), mFilenameExtension);
			Path filePath = mRootPath.resolve(fileName);

			Files.createFile(filePath, attr);
			gLogger.info("Test cases for invalid values of StorageAgentService APIs Started");
			gLogger.info("Test cases for invalid values of storage Allocation API Started");
			// Test storage Allocation API : for invalid values
			testMediaStorageAllocationApi_InvalidValues();
			gLogger.info("Test cases for invalid values of Media Check API Started");
			// Test Media Check API for Invalid values
			testCdvrStorageModuleMediacheck_InvalidValues();
			gLogger.info("Test cases for invalid values of Media Delete API Started");
			// Test Media Delete API from node for Invalid values.
			testCdvrStorageModuleMediaDelete_InvalidValues();
			gLogger.info("Test cases for invalid values of Media Check API Started");
			// Test Cancel Storage Allocation.
			testStorageModuleStorageCancel_InvalidValues();
			gLogger.info("Test cases for invalid values of StorageAgentService APIs completed");

			// TODO add test for max Concurrent upload.
		}

		/**
		 * This method will test all flows related to storage allocations in memory.
		 * 
		 * @throws Exception
		 */
		@Test
		public void testStorageAllocationsFlow() throws Exception {
			CdvrStorageModuleStatus storageModuleResult = testCdvrStorageModuleMediaGet();
			long cummulativeMediaSize = 0;
			mAvailableStorageForAllocation = storageModuleResult
					.getFreeDiskSpaceKB();
			long availableStorageBeforeAllocation = storageModuleResult
					.getUnallocatedDiskSpaceKB();
			gLogger.info("Test begins available storage before Allocation  {}",
					mAvailableStorageForAllocation);
			gLogger.info(
					"Test begins getUnallocatedDiskSpaceKB storage before Allocation  {}",
					availableStorageBeforeAllocation);
			gLogger.info("Test begins allocated storage in memory {}",
					mAllocationSystem.getAllocatedStorage());

			storageMediaSize = (long) ((mAvailableStorageForAllocation) * .10);
			gLogger.info("Media Size {}", storageMediaSize);
			cummulativeMediaSize = storageMediaSize;
			URL storagePath = testCdvrStorageModuleMediaAllocate(storageMediaSize,
					"Testupload1");
			long allocatedStorage = mAllocationSystem.getAllocatedStorage();
			assertEquals(allocatedStorage, cummulativeMediaSize);
			assertTrue("Storage Path should contain media1 in path directory. ",storagePath.toString().contains("/media1"));
			gLogger.info(
					"Allocated Storage for media named Testupload1 and size {}",
					storageMediaSize);

			gLogger.info("allocated storage in memory {}",
					mAllocationSystem.getAllocatedStorage());
			cummulativeMediaSize = cummulativeMediaSize + storageMediaSize * 3;
			storagePath = testCdvrStorageModuleMediaAllocate(storageMediaSize * 3,
					"Testupload2");
			allocatedStorage = mAllocationSystem.getAllocatedStorage();
			gLogger.info(
					"Allocated Storage for media named Testupload2 and size {}",
					(storageMediaSize * 3));

			gLogger.info("allocated storage in memory {}", allocatedStorage);
			assertEquals(allocatedStorage, cummulativeMediaSize);

			gLogger.info("Cancel allocated storage in memory named Testupload2 ");

			testStorageAllocationCancel(storagePath);
			cummulativeMediaSize = cummulativeMediaSize - storageMediaSize * 3;
			allocatedStorage = mAllocationSystem.getAllocatedStorage();
			assertEquals(allocatedStorage, cummulativeMediaSize);
			gLogger.info("Allocated storage in memory after cancellation{}",
					allocatedStorage);
			gLogger.info("Test completess for in memory allocations");

		}

		@Test
		public void testAllocationandFileCreation() throws Exception {
			gLogger.info("starting test to allocate storage then create file on disk for that upload name.");

			CdvrStorageModuleStatus storageModuleResult = testCdvrStorageModuleMediaGet();
			mAvailableStorageForAllocation = storageModuleResult
					.getFreeDiskSpaceKB();
			storageMediaSize = (long) ((mAvailableStorageForAllocation) * .10);
			URL storagePath = testCdvrStorageModuleMediaAllocate(storageMediaSize,
					"Testupload3");
			assertTrue("Storage Path should contain media1 in path directory. ",storagePath.toString().contains("/media1"));
			// Create file in storage module
			String fileName = String.format("%s.%s",
					RandomStringUtils.randomAlphanumeric(16) + "_T3",
					mFilenameExtension);
			Set<PosixFilePermission> perms = PosixFilePermissions
					.fromString("rw-rw----");

			Path filePath = Paths.get(storagePath.getPath(), fileName);
			gLogger.info("Path where file should be created ",
					storagePath.toString());
			gLogger.info("PAth for file ", filePath);
			FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions
					.asFileAttribute(perms);
			testCdvrStorageModuleMediaCheck(storagePath);

			testCdvrStorageModuleMediaDelete(storagePath);
			testStorageAllocationCancel(storagePath);
			// checking that folder is deleted and it should return error while trying to delete or check storagePath.
			Path path = Paths.get(
					mStorageAgentConfig.getMediaDiskRootDirectoryPathname(),
					FilenameUtils.getName(storagePath.toString()));
			assertFalse(Files.exists(path, new LinkOption[] { LinkOption.NOFOLLOW_LINKS }));
			ResponseEntity<String> response = null;
			CdvrStorageModuleMediaCheck storageModuleMediaCheck = new CdvrStorageModuleMediaCheck();
			storageModuleMediaCheck.setStoragePath(storagePath);
			response = mStorageAgentClient.invokeCdvrStorageMediaCheck(
					storageModuleMediaCheck, getBaseUrl());
			assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
			testCdvrStorageModuleMediaDelete_InvalidValues();
			CdvrStorageModuleMediaDelete cdvrStorageModuleMediaDelete = new CdvrStorageModuleMediaDelete();
			cdvrStorageModuleMediaDelete.setMediaPath(storagePath);
			ResponseEntity<String> responseDel = null;

			cdvrStorageModuleMediaDelete.setMediaPath(null);
			responseDel = mStorageAgentClient.invokeCdvrStorageMediaModuleDelete(
					cdvrStorageModuleMediaDelete,getBaseUrl());
			assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
			gLogger.info("Test complete for Allocation and File Creation cycle.");

		}

		/**
		 * <p>
		 * This tests runs following scenario.
		 * </p>
		 * <ul>
		 * <li>a) Store Media on storage Node.</li>
		 * <li>b) Delete Media on storage Node.</li>
		 * </ul>
		 * 
		 * @throws Exception
		 */
		@Test
		public void testStorageModuleFileApiFlow() throws Exception {
			Set<PosixFilePermission> perms = PosixFilePermissions
					.fromString("rw-r-----");
			FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions
					.asFileAttribute(perms);

			String fileName = String.format("%s.%s",
					RandomStringUtils.randomAlphanumeric(16), mFilenameExtension);
			Path filePath = Paths.get(
					mStorageAgentConfig.getMediaDiskRootDirectoryPathname(),
					fileName);
			Files.createFile(filePath, attr);
			URL url = new URL(getBaseUrl() + filePath);
			gLogger.info("File created at URL{} ", url);
			
			// Now Media file is created, so these testcases are called to check the
			// valid scenarios.
			testCdvrStorageModuleMediaCheck(url);
			testCdvrStorageModuleMediaDelete(url);
			gLogger.info("Test cases for valid url for Media check and Media delete are completed");

		}

		/**
		 * This method is used to test API cdvrStorageModuleMediaDelete.
		 * 
		 * @throws Exception
		 */
		private void testCdvrStorageModuleMediaDelete(URL url) throws Exception {
			ResponseEntity<String> response = null;

			CdvrStorageModuleMediaDelete storageModuleDelete = new CdvrStorageModuleMediaDelete();
			storageModuleDelete.setMediaPath(url);

			response = mStorageAgentClient.invokeCdvrStorageMediaModuleDelete(
					storageModuleDelete, getBaseUrl());

			assertEquals(HttpStatus.OK, response.getStatusCode());

			Success result = null;
			try {
				result = (Success) TrioUtil.jsonToTrio(response.getBody(),
						getSchemaVersion());
			} catch (IOException e) {
				gLogger.error("Failed to invoke cdvrStorageModuleMediaDelete API");
				assertTrue(false);
			}
			assertNotNull(result);

		}

		private void testStorageAllocationCancel(URL url) throws Exception {
			ResponseEntity<String> response = null;
			CdvrStorageModuleStorageCancel moduleStorageCancel = new CdvrStorageModuleStorageCancel();
			moduleStorageCancel.setStoragePathRoot(url);
			Success result = null;
			try {
				response = mStorageAgentClient.cdvrStorageModuleStorageCancel(
						moduleStorageCancel, getBaseUrl());
				assertEquals(HttpStatus.OK, response.getStatusCode());
				result = (Success) TrioUtil.jsonToTrio(response.getBody(),
						getSchemaVersion());
			} catch (IOException e) {
				gLogger.error("Failed to invoke Cancel Allocation API");
				assertTrue(false);
			}
		}

		private void testStorageModuleStorageCancel_InvalidValues()
				throws Exception {
			ResponseEntity<String> response = null;
			com.tivo.tws.schema.beans.Error result = null;

			CdvrStorageModuleStorageCancel storageCancel = new CdvrStorageModuleStorageCancel();

			// Test null URL
			storageCancel.setStoragePathRoot(null);
			response = mStorageAgentClient.cdvrStorageModuleStorageCancel(
					storageCancel, getBaseUrl());
			assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
			result = (com.tivo.tws.schema.beans.Error) TrioUtil.jsonToTrio(
					response.getBody(), getSchemaVersion());

			// Test invalid URL
			storageCancel.setStoragePathRoot(new URL("ftp://testfile/wrongURL"));
			response = mStorageAgentClient.cdvrStorageModuleStorageCancel(
					storageCancel, getBaseUrl());
			assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
			result = (com.tivo.tws.schema.beans.Error) TrioUtil.jsonToTrio(
					response.getBody(), getSchemaVersion());

			// Test URL doesn't exits
			storageCancel.setStoragePathRoot(new URL(getBaseUrl() + "/testfile"));
			response = mStorageAgentClient.cdvrStorageModuleStorageCancel(
					storageCancel, getBaseUrl());
			assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
			result = (com.tivo.tws.schema.beans.Error) TrioUtil.jsonToTrio(
					response.getBody(), getSchemaVersion());

			assertNotNull(result);
		}


}
