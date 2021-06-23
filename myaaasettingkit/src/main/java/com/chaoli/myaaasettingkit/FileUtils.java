package com.chaoli.myaaasettingkit;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

public class FileUtils {
    /**
     * PCM文件转WAV文件
     * @param inPcmFilePath 输入PCM文件路径
     * @param outWavFilePath 输出WAV文件路径
     * @param sampleRate 采样率，例如44100
     * @param channels 声道数 单声道：1或双声道：2
     * @param bitNum 采样位数，8或16
     */
    public static void convertPcm2Wav(String inPcmFilePath, String outWavFilePath, int sampleRate,
                                      int channels, int bitNum) {

        FileInputStream in = null;
        FileOutputStream out = null;
        byte[] data = new byte[1024];

        try {
            //采样字节byte率
            long byteRate = sampleRate * channels * bitNum / 8;

            in = new FileInputStream(inPcmFilePath);
            out = new FileOutputStream(outWavFilePath);

            //PCM文件大小
            long totalAudioLen = in.getChannel().size();

            //总大小，由于不包括RIFF和WAV，所以是44 - 8 = 36，在加上PCM文件大小
            long totalDataLen = totalAudioLen + 36;

            writeWaveFileHeader(out, totalAudioLen, totalDataLen, sampleRate, channels, byteRate);

            int length = 0;
            while ((length = in.read(data)) > 0) {
                out.write(data, 0, length);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static boolean checkPCMFile(String path){

        File file = new File(path);
        if (file.exists()) {
            return true;
        }
        return false;
    }

    /**
     * 输出WAV文件
     * @param out WAV输出文件流
     * @param totalAudioLen 整个音频PCM数据大小
     * @param totalDataLen 整个数据大小
     * @param sampleRate 采样率
     * @param channels 声道数
     * @param byteRate 采样字节byte率
     * @throws IOException
     */
    private static void writeWaveFileHeader(FileOutputStream out, long totalAudioLen,
                                            long totalDataLen, int sampleRate, int channels, long byteRate) throws IOException {
        byte[] header = new byte[44];
        header[0] = 'R'; // RIFF
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);//数据大小
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';//WAVE
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        //FMT Chunk
        header[12] = 'f'; // 'fmt '
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';//过渡字节
        //数据大小
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        //编码方式 10H为PCM编码格式
        header[20] = 1; // format = 1
        header[21] = 0;
        //通道数
        header[22] = (byte) channels;
        header[23] = 0;
        //采样率，每个通道的播放速度
        header[24] = (byte) (sampleRate & 0xff);
        header[25] = (byte) ((sampleRate >> 8) & 0xff);
        header[26] = (byte) ((sampleRate >> 16) & 0xff);
        header[27] = (byte) ((sampleRate >> 24) & 0xff);
        //音频数据传送速率,采样率*通道数*采样深度/8
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        // 确定系统一次要处理多少个这样字节的数据，确定缓冲区，通道数*采样位数
        header[32] = (byte) (channels * 16 / 8);
        header[33] = 0;
        //每个样本的数据位数
        header[34] = 16;
        header[35] = 0;
        //Data chunk
        header[36] = 'd';//data
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
        out.write(header, 0, 44);
    }

    public synchronized static void writeFileToSDCard(@NonNull final byte[] buffer, final String folder,
                                                      final String fileName, final boolean append, final boolean autoLine,int sampleRate,int channel) {

//        File absoluteFile = getCacheDir().getAbsoluteFile();


        boolean sdCardExist = Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
        String folderPath = folder;
        if (sdCardExist) {
            //TextUtils为android自带的帮助类
            if (TextUtils.isEmpty(folder)) {
                //如果folder为空，则直接保存在sd卡的根目录
                folderPath = Environment.getExternalStorageDirectory()
                        + File.separator;
            } else {
                folderPath = Environment.getExternalStorageDirectory()
                        + File.separator + folder + File.separator;
            }
        } else {
            return;
        }


        Log.i("chaoli", "writeFileToSDCard: folderPath="+folderPath+fileName);
        File fileDir = new File(folderPath);
        if (!fileDir.exists()) {
            if (!fileDir.mkdirs()) {
                return;
            }
        }
        File file;
        //判断文件名是否为空
        if (TextUtils.isEmpty(fileName)) {
            file = new File(folderPath + "sample.wav");
        } else {
            file = new File(folderPath + fileName);
        }
        RandomAccessFile raf = null;
        FileOutputStream out = null;
        try {
            if (append) {

                long fileLength = file.length();
                long bufferLength = buffer.length;



                //如果为追加则在原来的基础上继续写文件
                raf = new RandomAccessFile(file, "rw");
                if(fileLength==0){
                    writeHeader(raf,bufferLength+36,bufferLength,sampleRate,channel,16);
                    raf.seek(44);
                }else{
                    writeHeader(raf,fileLength+bufferLength,fileLength+bufferLength-36,sampleRate,channel,16);
                    raf.seek(fileLength);
                }

                raf.write(buffer);

                //  如果你的资源需要换行,请自行打开 此处我是保存的音频文件
                //if (autoLine) {
                //    raf.write( "\n".getBytes() );
                // }
            } else {
                //重写文件，覆盖掉原来的数据
                out = new FileOutputStream(file);
                out.write(buffer);
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (raf != null) {
                    raf.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

//    private static void modifyHeader(RandomAccessFile raf, long l, long bufferLength) {
//
//    }

    private static void writeHeader(RandomAccessFile raf, long totalAudioLen,
                                    long totalDataLen, int sampleRate, int channels, long byteRate) throws IOException {
        byte[] header = new byte[44];
        header[0] = 'R'; // RIFF
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);//数据大小
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';//WAVE
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        //FMT Chunk
        header[12] = 'f'; // 'fmt '
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';//过渡字节
        //数据大小
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        //编码方式 10H为PCM编码格式
        header[20] = 1; // format = 1
        header[21] = 0;
        //通道数
        header[22] = (byte) channels;
        header[23] = 0;
        //采样率，每个通道的播放速度
        header[24] = (byte) (sampleRate & 0xff);
        header[25] = (byte) ((sampleRate >> 8) & 0xff);
        header[26] = (byte) ((sampleRate >> 16) & 0xff);
        header[27] = (byte) ((sampleRate >> 24) & 0xff);
        //音频数据传送速率,采样率*通道数*采样深度/8
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        // 确定系统一次要处理多少个这样字节的数据，确定缓冲区，通道数*采样位数
        header[32] = (byte) (channels * 16 / 8);
        header[33] = 0;
        //每个样本的数据位数
        header[34] = 16;
        header[35] = 0;
        //Data chunk
        header[36] = 'd';//data
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
        raf.seek(0);
        raf.write(header, 0, 44);
    }

//    private static final String TAG = "FileScan";
//    public HashMap<String, String> getMusicListOnSys(File file) {
//
//        //从根目录开始扫描
//        Log.i(TAG, file.getPath());
//        HashMap<String, String> fileList = new HashMap<String, String>();
//        getFileList(file, fileList);
//        return fileList;
//    }

//    /**
//     * @param path
//     * @param fileList
//     * 注意的是并不是所有的文件夹都可以进行读取的，权限问题
//     */
//    private HashMap<String, String> getFileList(String path){
//
//        File file = new File(path);
//        HashMap<String, String> fileList = new HashMap<String, String>();
//
//        //如果是文件夹的话
//        if(file.isDirectory()){
//            //返回文件夹中有的数据
//            File[] files = file.listFiles();
//            //先判断下有没有权限，如果没有权限的话，就不执行了
//            if(null == files)
//                return null;
//
//            for(int i = 0; i < files.length; i++){
//                getFileList(files[i], fileList);
//            }
//        }
//        //如果是文件的话直接加入
//        else{
//            Log.i(TAG, path.getAbsolutePath());
//            //进行文件的处理
//            String filePath = path.getAbsolutePath();
//            //文件名
//            String fileName = filePath.substring(filePath.lastIndexOf("/")+1);
//            //添加
//            fileList.put(fileName, filePath);
//        }
//    }


}
