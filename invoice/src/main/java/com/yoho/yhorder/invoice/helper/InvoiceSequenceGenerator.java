package com.yoho.yhorder.invoice.helper;

import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

/**
 * Created by chenchao on 2016/6/17.
 */
public final class InvoiceSequenceGenerator {


    private static Logger logger = LoggerFactory.getLogger(InvoiceSequenceGenerator.class);

    public static final String seperator = "_";
    private final static String[] letters = new String[]{"a","b","c","d","e","f","g","h","i",
            "j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z"};
    private final int[] digit = new int[]{0,1,2,3,4,5,6,7,8,9};

    /**
     *
     * @param parentSeq
     * @param seqIndex 遵从数组下标顺序，从0开始顺推
     * @return
     */
    public static String nextSequence(String parentSeq, int seqIndex){
        logger.debug("nextSequence in param parentSeq {}, seqIndex {}",parentSeq, seqIndex);
        String[] segment = parentSeq.split(seperator);
        int seq;
        if (NumberUtils.isNumber(segment[seqIndex])){
            seq = Integer.valueOf(segment[seqIndex]) + 1;
            segment[seqIndex] = String.valueOf(seq);
        }
        StringBuilder sb = new StringBuilder();
        for(int i=0; i< segment.length; i++){
            if (i == segment.length-1){
                sb.append(segment[i]);
                continue;
            }
            sb.append(segment[i]).append(seperator);
        }
        return sb.toString();
    }


    public static String newSequence(int orderId){
        int letterIndex = new Random().nextInt(26);
        logger.debug("InvoiceSequenceGenerator.newSequence letterIndex {}",letterIndex);
        return new StringBuilder(String.valueOf(1))
                .append(seperator)
                .append(letters[letterIndex])
                .append(orderId).toString();
    }


    public static void main(String[] args) {
        System.out.println(InvoiceSequenceGenerator.newSequence(123444));
        String parentSeq = "xxxyy_2_a234343";
        String next = InvoiceSequenceGenerator.nextSequence(parentSeq,1);
        System.out.println("next is "+ next);
    }
}
