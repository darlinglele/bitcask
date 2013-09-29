package com.pwc.bitcask;

import com.pwc.object.Serialization;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;

public class BitCask<T> {
    private final Indexer indexer;
    private final String name;
    private static HashMap<String, BitCask> bitCasks = new HashMap<>();
    private long offset;

    public static <T> BitCask<T> of(String name) {
        return of(name, defaultIndexOf(name));
    }

    public static <T> BitCask<T> of(String name, String indexFile) {
        if (bitCasks.containsKey(name)) {
            return (BitCask<T>) bitCasks.get(name);
        } else {
            BitCask<T> newBitCask = new BitCask<>(name, indexFile);
            bitCasks.put(name, newBitCask);
            return newBitCask;
        }
    }

    public void put(String key, T value) {
        byte[] bytes = convertObjectToBytes(value);
        if (appendValue(key, bytes)) {
            updateIndex(key, bytes, this.offset);
        }
    }

    public void dumpIndexTo(String indexFile) {
        RandomAccessFile file = getFileAccesser(indexFile);
        if (file != null) {
            try {
                file.write(convertObjectToBytes(this.indexer));
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (file != null) {
                    try {
                        file.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public T get(String key) {
        return readFromFile(this.indexer.get(key));
    }

    private static String defaultIndexOf(String name) {
        return name + ".index";
    }

    private BitCask(String name, String indexFile) {
        this.name = name;
        this.indexer = loadIndexFrom(indexFile);
    }


    private Indexer loadIndexFrom(String indexFile) {
        RandomAccessFile file = getFileAccesser(indexFile);
        try {
            if (file.length() == 0) {
                return new Indexer();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] bytes = null;
        try {
            bytes = readBytesFromFile(0, (int) file.length(), indexFile);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (file != null) {
                try {
                    file.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if (bytes == null) {
            return new Indexer();
        }
        Object object = convertBytesToObject(bytes);
        if (!(object instanceof Indexer))
            return new Indexer();
        else
            return (Indexer) object;
    }


    private boolean appendValue(String key, byte[] bytes) {
        return appendBytesToFile(bytes, this.name);
    }

    private boolean appendBytesToFile(byte[] bytes, String name) {
        RandomAccessFile file = getFileAccesser(name);
        try {
            long offset = file.length();
            file.seek(offset);
            file.write(bytes);
            this.offset = offset;
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (file != null) {
                try {
                    file.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        return false;
    }


    private RandomAccessFile getFileAccesser(String name) {
        try {
            return new RandomAccessFile(name, "rw");
        } catch (FileNotFoundException e) {
            e.printStackTrace();

        }
        return null;
    }

    private byte[] convertObjectToBytes(Object value) {
        try {
            return Serialization.serialize(value);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void updateIndex(String key, byte[] bytes, long offset) {
        this.indexer.put(key, new Index(key, this.name, offset, bytes.length));
    }


    private T readFromFile(Index index) {
        byte[] bytes = readBytesFromFile(index.offset, index.size, index.fileName);
        Object object = convertBytesToObject(bytes);
        return (T) object;
    }

    private byte[] readBytesFromFile(long offset, int size, String fileName) {
        byte[] bytes = new byte[size];
        RandomAccessFile file = getFileAccesser(fileName);
        try {
            file.seek(offset);
            file.read(bytes);
        } catch (IOException e) {
            e.printStackTrace();
            bytes = null;
        } finally {
            if (file != null) {
                try {
                    file.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return bytes;
    }

    private Object convertBytesToObject(byte[] bytes) {
        try {
            return Serialization.deserialize(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

}
