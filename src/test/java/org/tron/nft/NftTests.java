package org.tron.nft;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;

/**
 * NFT交易
 * @author Brian
 * @date 2021/6/28 16:26
 */
@Slf4j
public class NftTests {

    @Test
    public void test(){
        URL url = null;
        String address = "";
        BigInteger start = new BigInteger("201811231251139709");
        while (true) {
            try {
                //http://pdf.dfcfw.com/pdf/H3_AP201904191320896124_1.pdf
                address = "https://pdf.dfcfw.com/pdf/H3_AP" + start.toString() + "_1.pdf";
                //System.out.println("访问地址：" + address);
                url = new URL(address);
                Object obj = url.getContent();
                //System.out.println(obj.getClass().getName());
                if (StringUtils.isNotEmpty(obj.getClass().getName())) {
                    logger.info("访问地址：[" + address + "]存在对应的文件");
                }
            } catch (IOException e) {
                //logger.error("访问地址：[" + address + "]没有对应的文件");
                //e.printStackTrace();
            } finally {
                start = start.add(BigInteger.ONE);
            }
        }
    }

}
