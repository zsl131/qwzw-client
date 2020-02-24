package com.zslin.test;

import com.zslin.other.tools.CheckMemberTools;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Created by zsl on 2019/4/1.
 */
@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("zsl")
public class JDBCTest {

    @Test
    public void test01() throws Exception {

        CheckMemberTools cmt = new CheckMemberTools();
        cmt.run();
    }
}
