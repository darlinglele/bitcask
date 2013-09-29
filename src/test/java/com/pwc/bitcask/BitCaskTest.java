package com.pwc.bitcask;

import com.pwc.bitcask.test.Student;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class BitCaskTest {
    @Test
    public void should_return_the_object_has_been_putted() throws Exception {
        BitCask<Student> studentBitCask = BitCask.of("Student");
        int i = 0;
        while (i < 100000) {
            String key = String.valueOf(i);
            Student value = new Student("401612002", "name", "age", "hobbit");
            studentBitCask.put(key, value);
            assertThat(studentBitCask.get(key), is(value));
            i++;
        }
        studentBitCask.dumpIndexTo("Student.index");
    }


}
