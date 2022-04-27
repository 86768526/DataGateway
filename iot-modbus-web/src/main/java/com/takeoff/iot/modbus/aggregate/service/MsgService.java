package com.takeoff.iot.modbus.aggregate.service;

import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Random;
import java.util.Vector;

@Lazy(value = false)
@EnableScheduling
@Service(value="msgService")
public class MsgService {

    final Vector<HttpServletResponse> list_res= new Vector<HttpServletResponse>();

    public Vector<HttpServletResponse> getListRes()
    {
        return list_res;
    }

    @Scheduled(initialDelay = 0, fixedDelay = 3*1000)
    public void run() {
        this.removeErrorResponse();
        Iterator<HttpServletResponse> it = list_res.iterator();
        Random rand =new Random();
        int num=rand.nextInt(100);
        while(it.hasNext())
        {
            PrintWriter pw = null;
            try {
                pw = it.next().getWriter();
                if(pw == null || pw.checkError())
                {
                    continue;
                }
                pw.write("data:msg: hello, the random num is: " + num + "\n\n");
                pw.flush();
            } catch (Exception e) {

            }
        }
    }

    public synchronized void removeErrorResponse(){
        Iterator<HttpServletResponse> it = list_res.iterator();
        while(it.hasNext())
        {
            PrintWriter pw = null;
            try {
                pw = it.next().getWriter();
                if(pw == null)
                {
                    it.remove();
                    continue;
                }
                else if(pw.checkError()){
                    pw.close();
                    it.remove();
                    continue;
                }
            } catch (Exception e) {
                it.remove();
            }
        }
    }
}

