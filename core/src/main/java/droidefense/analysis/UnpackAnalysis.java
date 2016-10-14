package droidefense.analysis;

import apkr.external.modules.helpers.log4j.Log;
import apkr.external.modules.helpers.log4j.LoggerType;
import droidefense.analysis.base.AbstractAndroidAnalysis;
import droidefense.handler.*;
import droidefense.handler.base.AbstractHandler;
import droidefense.handler.base.DirScannerFilter;
import droidefense.sdk.helpers.DroidDefenseParams;
import droidefense.sdk.helpers.Util;
import droidefense.sdk.model.base.APKFile;
import droidefense.sdk.model.base.HashedFile;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by sergio on 16/2/16.
 */
public final class UnpackAnalysis extends AbstractAndroidAnalysis {

    private transient ArrayList<HashedFile> files;

    public UnpackAnalysis() {
        files = new ArrayList<>();
    }

    @Override
    public boolean analyze() {
        AbstractHandler handler;
        Log.write(LoggerType.TRACE, "Unpacking .apk...");
        //prepare folder
        File outputDir = FileIOHandler.getUnpackOutputFile(apkFile);
        if (outputDir.exists() && DroidDefenseParams.getInstance().OVERWRITE_DECODE_FOLDER) {
            //delete
            Util.deleteFolder(outputDir);
        }
        //unpack file
        switch (apkFile.getTechnique()) {
            case APKFile.APKTOOL: {
                //unpacks and decode on the same step
                handler = new APKToolHandler(apkFile, outputDir);
                handler.doTheJob();
                Log.write(LoggerType.TRACE, "Listing unpacked files...");

                //enumerate unpacked files and get information
                boolean generateHashes = true;
                DirScannerHandler dirHandler = new DirScannerHandler(outputDir, generateHashes, new DirScannerFilter() {
                    @Override
                    public boolean addFile(File f) {
                        return true;
                    }
                });
                dirHandler.doTheJob();
                files = dirHandler.getFiles();
                Log.write(LoggerType.TRACE, "Files found: " + files.size());

                //save files count
                currentProject.setFolderCount(dirHandler.getNfolder());
                currentProject.setFilesCount(dirHandler.getNfiles());
                positiveMatch = files != null && files.size() > 0;
                //SET APP FILES & CALCULATE THEIR HASHES, FUZZING HASH, EXTENSION, SIGNATURE
                currentProject.setAppFiles(getFiles());
                timeStamp.stop();
                break;
            }
            case APKFile.AXML: {
                //only unpacks
                handler = new FileUnzipHandler(apkFile, outputDir);
                handler.doTheJob();
                Log.write(LoggerType.TRACE, "Listing unpacked files...");

                //enumerate unpacked files and getAsDotGraph information
                boolean generateHashes = true;
                DirScannerHandler dirHandler = new DirScannerHandler(outputDir, generateHashes, new DirScannerFilter() {
                    @Override
                    public boolean addFile(File f) {
                        return true;
                    }
                });
                handler.doTheJob();

                //get extracted files
                files = dirHandler.getFiles();
                Log.write(LoggerType.TRACE, "Files found: " + files.size());

                //save files count
                currentProject.setFolderCount(dirHandler.getNfolder());
                currentProject.setFilesCount(dirHandler.getNfiles());
                positiveMatch = files != null && files.size() > 0;

                //SET APP FILES & CALCULATE THEIR HASHES, FUZZING HASH, EXTENSION, SIGNATURE
                currentProject.setAppFiles(getFiles());

                Log.write(LoggerType.TRACE, "Decoding XML resources");
                //decode unpacked files
                AXMLDecoderHandler decoder = new AXMLDecoderHandler(files);
                decoder.doTheJob();

                Log.write(LoggerType.TRACE, "Generating file juiciy information...");
                //getAsDotGraph hash, ssdeep, extension,... information

                timeStamp.stop();
                break;
            }
        }
        return true;
    }

    @Override
    public String getName() {
        return "Android .apk unpacker";
    }

    public ArrayList<HashedFile> getFiles() {
        if (files == null)
            new ArrayList<>();
        return files;
    }
}