package com.nec.fraktal.ifs.mvel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mvel2.MVEL;

public class MvelTest {
    private static Serializable compileExpression_;

    @BeforeClass
    public static void setUp() {
        String script = "put('updateWheres', new java.util.ArrayList());"
                + "updateWheres.add("
                + "new com.nec.fraktal.ifs.mvel.Provision("
                + "ENGINE_TABLE_ALIAS.get('gen2_load_inst'), 'dev_id', com.nec.fraktal.ifs.mvel.OperatorKind.EQUAL, ENGINE_EVENT_REQ[0].get('serial_id')))";
        compileExpression_ = MVEL.compileExpression(script);
    }

    @Test
    public void test() {

        ExecutorService pool = Executors.newFixedThreadPool(1);
        try {
            List<Future<?>> list = new ArrayList<>();
            for (int i = 0; i < 153; i++) {
              Future<?> future = pool.submit(new Runner());
              list.add(future);
            }
            for (Future<?> future : list) {
              try {
                future.get();
              } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
              }
            }
          } finally {
            pool.shutdownNow();
          }
    }

    public static class Runner implements Runnable {
        @Override
        public void run() {
            try {
//                for (int i = 0; i < 100; i++)  {
                    Map<String, Object> param = new HashMap<String, Object>();
                    Map<String, Object> engineTableAlias = new HashMap<String, Object>();
                    engineTableAlias.put("gen2_load_inst", "aaa");
                    param.put("ENGINE_TABLE_ALIAS", engineTableAlias);
                    Map<String, Object> engineEventReq = new HashMap<String, Object>();
                    engineEventReq.put("serial_d", "bbb");
                    List<Map<String, Object>> engineEventReqs = new ArrayList<>();
                    engineEventReqs.add(engineEventReq);
                    param.put("ENGINE_EVENT_REQ", engineEventReqs);


                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("__value__", null);
                    MVEL.executeExpression(compileExpression_, param, map);
                    Object value = param.get("updateWheres");
                    Assert.assertTrue(value instanceof ArrayList);
                    Assert.assertTrue(((ArrayList)value).get(0) instanceof Provision);
//                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
