package jnpf.util;

import java.io.*;
import java.util.HashMap;

public class DeepCopy {
    public static <T> T deepCopyViaSerialization(T obj) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            oos.flush();

            ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bis);
            return (T) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("深拷贝失败", e);
        }
    }

    // 使用示例
    HashMap<String, Object> originalMap = new HashMap<>();
    HashMap<String, Object> deepCopyMap = deepCopyViaSerialization(originalMap);

}
