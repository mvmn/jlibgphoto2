package x.mvmn.jlibgphoto2;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;

import com.sun.jna.ptr.LongByReference;
import com.sun.jna.ptr.PointerByReference;

import x.mvmn.gphoto2.jna.CameraFileInfo;
import x.mvmn.gphoto2.jna.Gphoto2Library;
import x.mvmn.gphoto2.jna.Gphoto2Library.CameraFileType;
import x.mvmn.jlibgphoto2.GP2Camera.GP2CameraEventType;
import x.mvmn.jlibgphoto2.util.GP2ErrorHelper;

public class GP2CameraFilesHelper {

	public static void main(String[] args) throws Exception {
		GP2Camera camera = new GP2Camera();
		for (int i = 0; i < 1; i++) {
			System.out.println(camera.capture());
			camera.waitForSpecificEvent(1000, GP2CameraEventType.CAPTURE_COMPLETE);
		}
		final JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.getContentPane().setLayout(new BorderLayout());
		frame.setPreferredSize(Toolkit.getDefaultToolkit().getScreenSize());
		JTabbedPane tabPane = new JTabbedPane();
		frame.getContentPane().add(tabPane, BorderLayout.CENTER);
		System.out.println("Folders:");
		for (CameraFileSystemEntryBean fsEntry : list(camera, "/", false, true, true)) {
			System.out.println(fsEntry);
		}
		System.out.println("Files:");
		for (CameraFileSystemEntryBean fsEntry : list(camera, "/", false, true)) {
			System.out.println(fsEntry);
			if (fsEntry.isFile()) {
				System.out.println(getFileInfo(camera, fsEntry.getPath(), fsEntry.getName()));
				if (fsEntry.getName().toLowerCase().endsWith(".jpg")) {
					final BufferedImage image = ImageIO.read(new ByteArrayInputStream(getCameraFileContents(camera, fsEntry.getPath(), fsEntry.getName())));
					tabPane.addTab(fsEntry.getPath() + fsEntry.getName(), new JLabel(new ImageIcon(image)));
				}
				deleteCameraFile(camera, fsEntry.getPath(), fsEntry.getName());
			}
		}
		frame.pack();
		frame.setVisible(true);
		Thread.sleep(1000);
		camera.close();
	}

	public static List<CameraFileSystemEntryBean> list(final GP2Camera camera, final String path) {
		return list(camera, path, true, false);
	}

	public static List<CameraFileSystemEntryBean> list(final GP2Camera camera, final String path, final boolean recursive) {
		return list(camera, path, true, recursive);
	}

	public static List<CameraFileSystemEntryBean> list(final GP2Camera camera, final String path, final boolean includeFolders, final boolean recursive) {
		return list(camera, path, true, includeFolders, recursive);
	}

	public static List<CameraFileSystemEntryBean> list(final GP2Camera camera, final String path, final boolean inclueFiles, final boolean includeFolders,
			final boolean recursive) {
		final List<CameraFileSystemEntryBean> result;
		final List<String> folders;
		if (includeFolders || recursive) {
			folders = internalList(camera, path, true);
		} else {
			folders = Collections.emptyList();
		}
		final List<String> files = inclueFiles ? internalList(camera, path, false) : Collections.emptyList();
		result = new ArrayList<CameraFileSystemEntryBean>((folders != null ? folders.size() : 0) + (files != null ? files.size() : 0));
		if (folders != null && (includeFolders || recursive)) {
			for (String folder : folders) {
				if (includeFolders) {
					result.add(new CameraFileSystemEntryBean(folder, path, true));
				}
				if (recursive) {
					result.addAll(list(camera, path + folder + "/", inclueFiles, includeFolders, true));
				}
			}
		}
		if (files != null) {
			for (String file : files) {
				result.add(new CameraFileSystemEntryBean(file, path, false));
			}
		}
		return result;
	}

	static List<String> internalList(final GP2Camera camera, final String path, boolean folders) {
		List<String> result;
		PointerByReference pbrListFiles = new PointerByReference();
		try {
			GP2ErrorHelper.checkResult(Gphoto2Library.INSTANCE.gp_list_new(pbrListFiles));
			pbrListFiles.setPointer(pbrListFiles.getValue());

			if (folders) {
				GP2ErrorHelper.checkResult(Gphoto2Library.INSTANCE.gp_camera_folder_list_folders(camera.getCameraByReference(), path, pbrListFiles,
						camera.getContext().getPointerByRef()));
			} else {
				GP2ErrorHelper.checkResult(Gphoto2Library.INSTANCE.gp_camera_folder_list_files(camera.getCameraByReference(), path, pbrListFiles,
						camera.getContext().getPointerByRef()));
			}

			int fileCount = GP2ErrorHelper.checkResult(Gphoto2Library.INSTANCE.gp_list_count(pbrListFiles));

			result = new ArrayList<String>(fileCount);
			for (int i = 0; i < fileCount; i++) {
				PointerByReference pbrFileName = new PointerByReference();
				GP2ErrorHelper.checkResult(Gphoto2Library.INSTANCE.gp_list_get_name(pbrListFiles, i, pbrFileName));
				final String fileName = pbrFileName.getValue().getString(0);
				result.add(fileName);
			}
		} finally {
			GP2ErrorHelper.checkResult(Gphoto2Library.INSTANCE.gp_list_unref(pbrListFiles));
		}
		return result;
	}

	// TODO: Make a wrapper for camera file info
	public static CameraFileInfoBean getFileInfo(final GP2Camera camera, final String path, final String fileName) {
		final PointerByReference pbrCameraFile = internalGetCameraFile(camera, path, fileName);
		CameraFileInfo.ByReference byRefCameraFileInfo;
		try {
			byRefCameraFileInfo = new CameraFileInfo.ByReference();
			GP2ErrorHelper.checkResult(Gphoto2Library.INSTANCE.gp_camera_file_get_info(camera.getCameraByReference(), path, fileName, byRefCameraFileInfo,
					camera.getContext().getPointerByRef()));
		} finally {
			internalFreeCameraFileSafely(pbrCameraFile);
		}
		return new CameraFileInfoBean(byRefCameraFileInfo);
	}

	public static byte[] getCameraFileContents(final GP2Camera camera, final String path, final String fileName) {
		final PointerByReference pbrCameraFile = internalGetCameraFile(camera, path, fileName);
		byte[] result;
		try {
			result = internalGetCameraFileData(pbrCameraFile);
		} finally {
			internalFreeCameraFileSafely(pbrCameraFile);
		}
		return result;
	}

	public static void deleteCameraFile(final GP2Camera camera, final String path, final String fileName) {
		GP2ErrorHelper.checkResult(
				Gphoto2Library.INSTANCE.gp_camera_file_delete(camera.getCameraByReference(), path, fileName, camera.getContext().getPointerByRef()));
	}

	static PointerByReference internalGetCameraFile(final GP2Camera camera, final String path, final String fileName) {
		final PointerByReference pbrCameraFile = new PointerByReference();
		GP2ErrorHelper.checkResult(Gphoto2Library.INSTANCE.gp_file_new(pbrCameraFile));
		pbrCameraFile.setPointer(pbrCameraFile.getValue());
		GP2ErrorHelper.checkResult(Gphoto2Library.INSTANCE.gp_camera_file_get(camera.getCameraByReference(), path, fileName, CameraFileType.GP_FILE_TYPE_NORMAL,
				pbrCameraFile, camera.getContext().getPointerByRef()));

		return pbrCameraFile;
	}

	static byte[] internalGetCameraFileData(PointerByReference cameraFile) {
		PointerByReference pref = new PointerByReference();
		LongByReference longByRef = new LongByReference();
		GP2ErrorHelper.checkResult(Gphoto2Library.INSTANCE.gp_file_get_data_and_size(cameraFile, pref, longByRef));
		return pref.getValue().getByteArray(0, (int) longByRef.getValue());
	}

	static void internalFreeCameraFileSafely(PointerByReference pbrFile) {
		Gphoto2Library.INSTANCE.gp_file_unref(pbrFile);
	}

}
