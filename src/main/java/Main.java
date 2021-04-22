import core.Debugger;
import core.MemoryInfo;
import io.net.NetworkConstants;
import io.net.SocketConnection;
import misc.DebuggerHelper;
import misc.ExpressionEvaluator;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Map;

public class Main {

    private static final long TID = 0x01006F8002326000L;

    public static void main(String[] args) throws Exception {

        System.out.println("ACNH dream dump start");
        Debugger debugger = new Debugger(new SocketConnection(DebuggerHelper.buildSocket(NetworkConstants.DEFAULT_HOST, NetworkConstants.DEFAULT_PORT, NetworkConstants.TIMEOUT)));
        Arrays.stream(debugger.getPids()).filter(pid -> TID == debugger.getTitleId(pid)).findFirst().ifPresent(debugger::attach);
        if (debugger.attached()) {
            MemoryInfo[] query = debugger.query(0, 10000);
            Map<String, Long> regionMap = DebuggerHelper.getRegionMap(query);
            ExpressionEvaluator expressionEvaluator = DebuggerHelper.expressionEvaluatorBuilder(regionMap, debugger);
            long mainAddr = expressionEvaluator.eval("[[[main+3D96720]+10]+130]+60");
            int mainSize = 0x527990;
            long startMilli = System.currentTimeMillis();
            try (OutputStream fileOutputStream = new FileOutputStream("main.dat")) {
                debugger.readmem(mainAddr, mainSize, fileOutputStream);
                System.out.println("Cost " + (System.currentTimeMillis() - startMilli) + "ms");
            }
        }
        System.out.println("Finish");
    }
}
