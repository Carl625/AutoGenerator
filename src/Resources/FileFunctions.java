package Resources;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileFunctions {

    /* --------- Files Processing and Statistics ---------*/

    public static File getFile(String directory) {

        return (new File(Paths.get(directory).toAbsolutePath().toString()));
    }

    public static File getFile(Path directory) {

        return (new File(directory.toAbsolutePath().toString()));
    }

    public static String getLocalPath(Path file, String rootFolder) {

        String fileString = file.toAbsolutePath().toString();
        return getLocalPath(fileString, rootFolder);
    }

    public static String getLocalPath(String fileString, String rootFolder) {


        if (fileString.contains(rootFolder)) {

            Pattern p = Pattern.compile(rootFolder);
            Matcher m = p.matcher(fileString);

            while (m.find()) {

                if ((m.start() == 0) || (m.start() != 0 && fileString.charAt(m.start() - 1) == '\\')) {

                    if ((m.end() == fileString.length()) || (m.end() != fileString.length() && fileString.charAt(m.end()) == '\\')) {

                        return fileString.substring(m.start());
                    }
                }
            }
        }

        return fileString;
    }

    public static boolean isLocallyCorrect(Path one, Path two, String commonFolder) {

        return isLocallyCorrect(one.toString(), two.toString(), commonFolder);
    }

    public static boolean isLocallyCorrect(String pathOne, String pathTwo, String commonFolder) {

        if (pathOne.contains(commonFolder) && pathTwo.contains(commonFolder)) {

            String firstPath = getLocalPath(pathOne, commonFolder);
            String secondPath = getLocalPath(pathTwo, commonFolder);
//			System.out.println(firstPath);
//			System.out.println(secondPath + "\n");
            return firstPath.equals(secondPath);
        }

        return false;
    }

    public static boolean directSubFile(Path directory, Path subFile) {

        String folder = directory.toAbsolutePath().toString();
        String file = subFile.toAbsolutePath().toString();

        return folder.equals(file.substring(0, file.indexOf(subFile.toFile().getName()) - 1));
    }

    public static boolean subFile(Path directory, Path subFile) {

        return subFile.toAbsolutePath().toString().contains(directory.toAbsolutePath().toString());
    }

    public static String getTopFile(String localizedPath) {

        if (localizedPath.endsWith("\\")) {

            String currentFolder = localizedPath.substring(0, localizedPath.lastIndexOf("\\"));
            currentFolder = currentFolder.substring(currentFolder.lastIndexOf("\\") + 1);

            return currentFolder;
        } else {

            String currentFile = localizedPath.substring(localizedPath.lastIndexOf("\\") + 1);

            return currentFile;
        }
    }

    private static ArrayList<String> findFilesStructureRoot(ArrayList<String> structure, String folder) {

        ArrayList<String> filesStructure = findFilesStructure(structure, folder);
        filesStructure = (ArrayList<String>) filesStructure.stream().map(s -> folder + "\\" + s).collect(Collectors.toList());

        return filesStructure;
    }

    private static ArrayList<String> findDirsStructureRoot(ArrayList<String> structure, String folder) {

        ArrayList<String> dirsStructure = findDirsStructureLocalDir(structure, folder);
        dirsStructure = (ArrayList<String>) dirsStructure.stream().map(s -> folder + "\\" + s).collect(Collectors.toList());

        return dirsStructure;
    }

    private static ArrayList<String> findDirsStructureLocalDir(ArrayList<String> structure, String folder) {

        ArrayList<String> directories = null;

        String rootStruct = null;

        for (String struct: structure) {

            if (struct.substring(0, struct.indexOf("<")).equals(folder)) {

                rootStruct = struct;
            }
        }

        if (rootStruct != null) {

            String[] contents = rootStruct.substring(rootStruct.indexOf("<") + 1, rootStruct.length() - 1).split(":");
            directories = (ArrayList<String>) Arrays.stream(contents).filter(s -> s.contains("FOLDER\\")).map(s -> s.substring(s.indexOf("\\") + 1, s.length())).collect(Collectors.toList());

            for (int directoryIndex = 0; directoryIndex < directories.size(); directoryIndex++) {

                String dirName = directories.get(directoryIndex);
                ArrayList<String> dirStructure = findDirsStructure(structure, dirName);
                dirStructure = (ArrayList<String>) dirStructure.stream().map(s -> (dirName + "\\" + s)).collect(Collectors.toList());
                directories.addAll(dirStructure);
            }
        }

        return directories;
    }

    private static ArrayList<String> findFilesStructure(ArrayList<String> structure, String folder) {

        ArrayList<String> files = null;

        String rootStruct = null;

        for (String struct: structure) {

            if (struct.substring(0, struct.indexOf("<")).equals(folder)) {

                rootStruct = struct;
            }
        }

        if (rootStruct != null) {

            String contentString = rootStruct.substring(rootStruct.indexOf("<") + 1, rootStruct.length() - 1);
            String[] contents = contentString.split(":");
            String[] subDirectories = Arrays.stream(contents).filter(s -> s.contains("FOLDER\\")).map(s -> s.substring(s.indexOf("\\") + 1, s.length())).toArray(String[]::new);

            if (contentString.length() > 0) {

                files = (ArrayList<String>) Arrays.stream(contents).filter(s -> !s.contains("FOLDER\\")).collect(Collectors.toList());
                for (String directory: subDirectories) {

                    ArrayList<String> subFiles = findFilesStructure(structure, directory);
                    subFiles = (ArrayList<String>) subFiles.stream().map(s -> (directory + "\\" + s)).collect(Collectors.toList());
                    files.addAll(subFiles);
                }
            } else {

                files = new ArrayList<String>();
            }
        }

        return files;
    }

    private static ArrayList<String> findDirsStructure(ArrayList<String> structure, String folder) {

        ArrayList<String> directories = null;

        String rootStruct = null;

        for (String struct: structure) {

            if (struct.substring(0, struct.indexOf("<")).equals(folder)) {

                rootStruct = struct;
            }
        }

        if (rootStruct != null) {

            String[] contents = rootStruct.substring(rootStruct.indexOf("<") + 1, rootStruct.length() - 1).split(":");
            directories = (ArrayList<String>) Arrays.stream(contents).filter(s -> s.contains("FOLDER\\")).map(s -> s.substring(s.indexOf("\\") + 1, s.length())).collect(Collectors.toList());

            for (int directoryIndex = 0; directoryIndex < directories.size(); directoryIndex++) {

                directories.addAll(findDirsStructure(structure, directories.get(directoryIndex)));
            }
        }

        return directories;
    }

    /* ---------- File Operations ---------- */

    public static ArrayList<String> readLines(Path file) {

        return readLines(file.toFile());
    }

    public static ArrayList<String> readLines(File file) {

        String thisLine = null;
        ArrayList<String> fileText = null;
        BufferedReader br = null;

        try {

            br = new BufferedReader(new FileReader(file));
            fileText = new ArrayList<String>();

            while ((thisLine = br.readLine()) != null) {
                fileText.add(thisLine);
            }
        } catch (Exception e) {

            e.printStackTrace();
        } finally {

            try {

                br.close();
            } catch (IOException e) {

                e.printStackTrace();
            } finally {

                br = null;
                System.gc();
            }
        }

        return fileText;
    }

    public static void writeLines(String writeFile, String longLine) {

        ArrayList<String> splitLines = new ArrayList<String>();

        // split lines by (expirimental?) value (can't use .split(), needs an identifier to remove)
        // nah I'll just write it as one line for now and see what happens
        splitLines.add(longLine);

        writeLines(writeFile, splitLines);
    }

    public static void writeLines(String writeFile, ArrayList<String> lines) {

        BufferedWriter bw = null;

        try {

            bw = new BufferedWriter(new FileWriter(writeFile));

            for (String str: lines) {

                bw.write(str);
                bw.newLine();
            }

            bw.flush();
        } catch (IOException e) {

            e.printStackTrace();
        } finally {

            try {

                bw.close();
            } catch (IOException e) {

                e.printStackTrace();
            } finally {

                bw = null;
                System.gc();
            }
        }
    }

    public static void deleteDirectory(Path dir) {

        if (dir != null && dir.toFile().exists() && dir.toFile().isDirectory()) {

            ArrayList<File> dirFiles = getFilesInDir(dir);
            ArrayList<File> subDirectories = getDirsInDir(dir);

            for (File file: dirFiles) {

                file.delete();
            }

            for (File directory: subDirectories) {

                deleteDirectory(directory.toPath());
            }

            File directory = dir.toFile();
            directory.delete();
        }
    }

    public static ArrayList<File> findAllFiles(Path dir) {

        ArrayList<File> files = getFilesInDir(dir);
        ArrayList<File> subDirectories = getDirsInDir(dir);

        for (int directory = 0; directory < subDirectories.size(); directory++) {

            files.addAll(findAllFiles(subDirectories.get(directory).toPath().toAbsolutePath()));
        }

        return files;
    }

    public static ArrayList<String> findAllFilesString(Path dir) {

        ArrayList<File> files = getFilesInDir(dir);
        ArrayList<File> subDirectories = getDirsInDir(dir);

        ArrayList<String> fileNames = (ArrayList<String>) files.stream().map(f -> getLocalPath(f.toPath(), dir.toFile().getName())).collect(Collectors.toList());

        for (int directory = 0; directory < subDirectories.size(); directory++) {

            ArrayList<String> subFiles = findAllFilesString(subDirectories.get(directory).toPath().toAbsolutePath());
            subFiles = (ArrayList<String>) subFiles.stream().map(s -> dir.toFile().getName() + "\\" + s).collect(Collectors.toList());
            fileNames.addAll(subFiles);
        }

        return fileNames;
    }

    public static ArrayList<File> findAllDirectories(Path dir) {

        ArrayList<File> allSubDirectories = getDirsInDir(dir);
        File[] subDirectories = Arrays.copyOf(allSubDirectories.toArray(new File[allSubDirectories.size()]), allSubDirectories.size());

        for (int directory = 0; directory < subDirectories.length; directory++) {

            allSubDirectories.addAll(findAllDirectories(subDirectories[directory].toPath().toAbsolutePath()));
        }

        return allSubDirectories;
    }

    public static ArrayList<File> findAllContents(Path dir) {

        ArrayList<File> allContents = getDirContents(dir);

        File[] contents = Arrays.copyOf(allContents.toArray(new File[allContents.size()]), allContents.size());

        for (int file = 0; file < contents.length; file++) {

            if (contents[file].isDirectory()) {

                allContents.addAll(findAllContents(contents[file].toPath().toAbsolutePath()));
            }
        }

        return allContents;
    }

    public static ArrayList<File> getFilesInDir(Path dir) {

        ArrayList<File> files = getDirContents(dir);
        files = (ArrayList<File>) files.stream().filter(f -> !f.isDirectory()).collect(Collectors.toList());

        return files;
    }

    public static ArrayList<File> getDirsInDir(Path dir) {

        ArrayList<File> files = getDirContents(dir);
        files = (ArrayList<File>) files.stream().filter(f -> f.isDirectory()).collect(Collectors.toList());

        return files;
    }

    public static ArrayList<File> getDirContents(Path dir) {

        Stream<Path> filePaths = Stream.empty();

        if (dir.toFile().exists()) {
            try {

                filePaths = Files.list(dir);
            } catch (IOException e) {

                e.printStackTrace();
            }
        }

        ArrayList<File> files = (ArrayList<File>) filePaths.map(p -> new File(p.toAbsolutePath().toString())).collect(Collectors.toList());

        return files;
    }

    public static void copyFile(File f, Path output) {

        copyFile(f.toPath().toAbsolutePath(), output);
    }

    public static void copyFile(Path source, Path output) {

        if (source.toFile().isDirectory()) {

            if (!output.toAbsolutePath().toFile().exists()) {

                makeDir(output.toFile());
            }
        } else {

            if (!output.toAbsolutePath().toFile().exists()) {

                makeFile(output.toAbsolutePath().toFile());
            }


            File image = new File(source.toAbsolutePath().toString());
            BasicFileAttributeView attributes = Files.getFileAttributeView(source, BasicFileAttributeView.class);
            FileTime creationTime = null;
            FileTime modifiedTime = null;

            try {

                creationTime = attributes.readAttributes().creationTime();
                modifiedTime = attributes.readAttributes().lastModifiedTime();
            } catch (IOException e) {
                e.printStackTrace();
            }

            byte[] fileContents = new byte[0];

            try {

                fileContents = Files.readAllBytes(Paths.get(image.getAbsolutePath()));
            } catch (IOException e) {

                e.printStackTrace();
            }

            try (FileOutputStream fos = new FileOutputStream(output.toAbsolutePath().toString())) {

                fos.write(fileContents);
                fos.flush();
            } catch (IOException e) {

                e.printStackTrace();
            } finally {

                try {

                    setFileCreationDate(output.toAbsolutePath().toString(), modifiedTime, creationTime);
                } catch (IOException e) {

                    e.printStackTrace();
                }
            }
        }
    }

    public static byte[] getFileContents(File f) {

        return getFileContents(Paths.get(f.getAbsolutePath()));
    }

    public static byte[] getFileContents(Path filePath) {

        byte[] fileContents = new byte[0];

        try {
            fileContents = Files.readAllBytes(filePath);
        } catch (IOException e) {

            e.printStackTrace();
        }

        return fileContents;
    }

    public static void makeFile(File f) {

        try {

            f.createNewFile();
        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    public static void makeDir(File f) {

        f.mkdirs();
    }

    public static void clearFile(File textFile) {

        try {

            RandomAccessFile r = new RandomAccessFile(textFile, "rw");
            r.setLength(0);
            r.close();
        } catch (FileNotFoundException e) {

            e.printStackTrace();
        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    public static void clearDir(Path dir) {

        if (dir != null && dir.toFile().exists() && dir.toFile().isDirectory()) {

            ArrayList<File> dirFiles = getFilesInDir(dir);
            ArrayList<File> subDirectories = getDirsInDir(dir);

            for (File f: dirFiles) {

                f.delete();
            }

            for (File subDir: subDirectories) {

                clearDir(subDir.toPath().toAbsolutePath());
            }
        }
    }

    public static void setFileCreationDate(String filePath, FileTime modifiedDate, FileTime creationDate) throws IOException {

        BasicFileAttributeView attributes = Files.getFileAttributeView(Paths.get(filePath), BasicFileAttributeView.class);
        FileTime creationTime = FileTime.from(creationDate.toInstant());
        FileTime modifiedTime = FileTime.from(modifiedDate.toInstant());
        attributes.setTimes(modifiedTime, attributes.readAttributes().lastAccessTime(), creationTime);
    }

    public static boolean CopyFull(ArrayList<File> files, Path output, String commonFolder) {

        boolean properlyCopied = true;

        for (File f: files) {
            Path newPath = Paths.get(output.toString(), "\\", getLocalPath(f.toPath(), commonFolder));
            Path newPathFolder = Paths.get(newPath.toString().substring(0, newPath.toString().lastIndexOf("\\")));
            makeDir(newPathFolder.toFile());
            copyFile(f, newPath);

            BasicFileAttributeView attrOG = Files.getFileAttributeView(f.toPath(), BasicFileAttributeView.class);
            BasicFileAttributeView attrNEW = Files.getFileAttributeView(newPath, BasicFileAttributeView.class);

            FileTime creationDateOG = null;
            FileTime creationDateNEW = null;
            FileTime lastModDateOG = null;
            FileTime lastModDateNEW = null;

            try {
                creationDateOG = attrOG.readAttributes().creationTime();
                creationDateNEW = attrNEW.readAttributes().creationTime();
                lastModDateOG = attrOG.readAttributes().lastModifiedTime();
                lastModDateNEW = attrNEW.readAttributes().lastModifiedTime();

            } catch (IOException e) {

                e.printStackTrace();
            }

            if (!creationDateOG.equals(creationDateNEW) || !lastModDateOG.equals(lastModDateNEW)) {

                properlyCopied = false;
            }
        }

        return properlyCopied;
    }

    public static boolean writeFileContent(File f) {

        return writeFileContents(getFileContents(f), f.getAbsolutePath());
    }

    public boolean writeFileContents(byte[] fileContents, Path filePath) {

        return writeFileContents(fileContents, filePath.toString());
    }

    public static boolean writeFileContents(byte[] fileContents, String filePath) {

        boolean completed = true;

        FileOutputStream fos = null;

        try {

            fos = new FileOutputStream(filePath);
        } catch (FileNotFoundException e1) {

            e1.printStackTrace();
        }

        try  {

            fos.write(fileContents);
        } catch (IOException e) {

            e.printStackTrace();
            completed = false;
        } finally {

            try {

                fos.close();
            } catch (IOException e) {

                e.printStackTrace();
            }
        }

        return completed;
    }
}
