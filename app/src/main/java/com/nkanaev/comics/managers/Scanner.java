package com.nkanaev.comics.managers;

import java.io.File;
import java.util.*;

import android.os.AsyncTask;
import com.nkanaev.comics.model.*;
import com.nkanaev.comics.parsers.Parser;
import com.nkanaev.comics.parsers.ParserFactory;

public class Scanner extends AsyncTask<Void, Void, Void> {
    private Storage mStorage;
    private DirectoryIterator mDirIterator;

    public enum Mode {
        CREATE,
        REFRESH,
    }

    public Scanner(Storage storage, File rootDir) {
        mStorage = storage;
        mDirIterator = new DirectoryIterator(rootDir);
    }

    @Override
    protected Void doInBackground(Void... params) {
        ArrayList<Comic> allComics = mStorage.listComics();
        HashMap<File, Comic> dirComics = new HashMap<>();
        for (Comic c : allComics) {
            dirComics.put(c.getFile(), c);
        }

        for (File file : mDirIterator) {
            if (isCancelled()) return null;

            if (dirComics.containsKey(file)) {
                dirComics.remove(file);
                continue;
            }

            Parser parser = ParserFactory.create(file);
            if (parser == null) continue;
            if (parser.numPages() > 0) {
                mStorage.addBook(file, parser.getType(), parser.numPages());
            }
        }

        for (Comic missing : dirComics.values()) {
            mStorage.removeComic(missing.getId());
        }

        return null;
    }

    private class DirectoryIterator implements Iterable<File>, Iterator<File> {
        Deque<File> mFiles = new ArrayDeque<>();

        public DirectoryIterator(File root) {
            mFiles.push(root);
        }

        @Override
        public Iterator<File> iterator() {
            return this;
        }

        @Override
        public boolean hasNext() {
            return !mFiles.isEmpty();
        }

        @Override
        public File next() {
            File f = mFiles.pop();
            if (f.isDirectory()) {
                mFiles.addAll(Arrays.asList(f.listFiles()));
            }
            return f;
        }

        @Override
        public void remove() {

        }
    }
}
