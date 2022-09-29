import core.Debugger;
import core.MemoryInfo;
import core.MemoryType;
import io.net.NetworkConstants;
import io.net.SocketConnection;
import misc.DebuggerHelper;

import java.io.PrintWriter;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Splatoon3Main {

    private static final long TID = 0x0100C2500FC20000L;

    public static void main(String[] args) throws Exception {
        if (args.length != 1 || !args[0].matches("(\\d+\\.){3}\\d+")) {
            System.out.println("请填入有效ip地址");
            return;
        }

        System.out.println("你输入的ip地址为：" + args[0]);
        System.out.println("开始找地址");

        try (Debugger debugger = new Debugger(new SocketConnection(DebuggerHelper.buildSocket(args[0], NetworkConstants.DEFAULT_PORT, NetworkConstants.TIMEOUT)))) {
            List<Long> pids = Arrays.stream(debugger.getPids()).boxed().collect(Collectors.toList());
            Collections.reverse(pids);
            pids.stream().filter(pid -> TID == debugger.getTitleId(pid)).findFirst().ifPresent(debugger::attach);
            if (debugger.attached()) {
                debugger.resume();
                MemoryInfo[] query = debugger.query(0, 10000);
                long heap1 = -1;
                for (MemoryInfo memoryInfo : query) {
                    if (memoryInfo.getType() == MemoryType.HEAP && memoryInfo.getSize() == 0x14B6000) {
                        heap1 = memoryInfo.getAddress();
                    }
                }
                if (heap1 == -1) {
                    System.out.println("没找到");
                    return;
                }
                long money1Address = heap1 + 0x1092E4;
                long money2Address = heap1 + 0x20D3C4;
                long money1 = debugger.readmem(money1Address, 4, new byte[4]).order(ByteOrder.LITTLE_ENDIAN).getInt() & 0xffffffffL;
                long money2 = debugger.readmem(money2Address, 4, new byte[4]).order(ByteOrder.LITTLE_ENDIAN).getInt() & 0xffffffffL;
                if (money1 == money2) {
                    System.out.println("请核对你的金币数是否为" + money1);
                    System.out.printf("金币地址1：0x%x%n金币地址1：0x%x%n", money1Address, money2Address);
                    PrintWriter pw = new PrintWriter("splatoon3_money.json");
                    pw.println(String.format(JSON, money1Address, money2Address));
                    pw.close();
                } else {
                    System.out.println("情况不妙，数据似乎不对");
                    return;
                }
            }
        }
        System.out.println("完事，准备用noexes导入生成的splatoon3_money.json");
    }

    private static final String JSON = "[\n" +
            "  {\n" +
            "    \"update\": true,\n" +
            "    \"locked\": false,\n" +
            "    \"addr\": \"%x\",\n" +
            "    \"desc\": \"money1\",\n" +
            "    \"type\": \"INT\",\n" +
            "    \"value\": 4700\n" +
            "  },\n" +
            "  {\n" +
            "    \"update\": true,\n" +
            "    \"locked\": false,\n" +
            "    \"addr\": \"%x\",\n" +
            "    \"desc\": \"money2\",\n" +
            "    \"type\": \"INT\",\n" +
            "    \"value\": 4700\n" +
            "  }\n" +
            "]";
}
