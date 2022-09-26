import core.Debugger;
import core.MemoryInfo;
import core.MemoryType;
import io.net.NetworkConstants;
import io.net.SocketConnection;
import misc.DebuggerHelper;

import java.util.Arrays;

public class Splatoon3Main {

    private static final long TID = 0x0100C2500FC20000L;

    public static void main(String[] args) throws Exception {
        if (args.length != 1 || !args[0].matches("(\\d+\\.){3}\\d+")) {
            System.out.println("请填入有效ip地址");
            return;
        }

        System.out.println("你输入的ip地址为：" + args[0]);
        System.out.println("开始找地址");
        Debugger debugger = new Debugger(new SocketConnection(DebuggerHelper.buildSocket(args[0], NetworkConstants.DEFAULT_PORT, NetworkConstants.TIMEOUT)));
        Arrays.stream(debugger.getPids()).filter(pid -> TID == debugger.getTitleId(pid)).findFirst().ifPresent(debugger::attach);
        try {
            if (debugger.attached()) {
                MemoryInfo[] query = debugger.query(0, 10000);
                long heap1 = 0;
                for (MemoryInfo memoryInfo : query) {
                    if (memoryInfo.getType() == MemoryType.HEAP && memoryInfo.getSize() == 0x14B6000) {
                        heap1 = memoryInfo.getAddress();
                    }
                }
                if (heap1 == 0) {
                    System.out.println("没找到");
                }
                long money1 = heap1 + 0x1092E4;
                long money2 = heap1 + 0x20D3C4;
                System.out.printf("金币地址1：0x%x%n金币地址1：0x%x%n", money1, money2);
            }
        } finally {
            debugger.close();
        }
        System.out.println("完事");
    }
}
