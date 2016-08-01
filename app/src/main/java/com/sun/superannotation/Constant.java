package com.sun.superannotation;


import com.just.SDCardRootFile;

/**
 * Created by walkingMen on 2016/8/1.
 */
public class Constant {
    //=================SD文件路径===========================================
    public static final String DISK_IMAGE_PHOTO_PATH = "img";

    //缓存路径
    @SDCardRootFile(fileNames = {DISK_IMAGE_PHOTO_PATH})
    public static final String CACHE_ROOT_DIR_NAME = "Xingyun";

}
