package com.yoho.yhorder.shopping.charge;

import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

/**
 * Created by wujiexiang on 16/3/16.
 */
public class MockTest {

    @Test
    public void test()
    {
        Iterator i = mock(Iterator.class );

        when(i.next()).thenReturn("Hello" ).thenReturn( "World" );
        //act
        String result=i.next()+" " +i.next();
        //verify
        verify(i, times(2 )).next();
        //assert
        assertEquals("Hello World" , result);
    }
}
