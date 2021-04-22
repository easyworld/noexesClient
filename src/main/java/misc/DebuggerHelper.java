package misc;

import core.Debugger;
import core.MemoryInfo;
import core.MemoryType;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class DebuggerHelper {
    public static Socket buildSocket(String host, int port, int timeout) throws Exception {
        Socket s = new Socket();
        InetSocketAddress addr = new InetSocketAddress(host, port);
        s.connect(addr, timeout);
        s.setTcpNoDelay(true);
        return s;
    }

    public static Map<String, Long> getRegionMap(MemoryInfo[] infos) {
        Map<String, Long> result = new HashMap<>();
        int moduleCount = 0;

        for (MemoryInfo m : infos) {
            if (m.getType() == MemoryType.CODE_STATIC && m.getPerm() == 0b101) {
                moduleCount++;
            }
        }
        int mod = 0;
        boolean heap = false;
        for (MemoryInfo m : infos) {
            String name = "-";
            if (m.getType() == MemoryType.HEAP && !heap) {
                heap = true;
                name = "heap";
            }
            if (m.getType() == MemoryType.CODE_STATIC && m.getPerm() == 0b101) {
                if (mod == 0 && moduleCount == 1) {
                    name = "main";
                }
                if (moduleCount > 1) {
                    switch (mod) {
                        case 0:
                            name = "rtld";
                            break;
                        case 1:
                            name = "main";
                            break;
                        case 2:
                            name = "sdk";
                            break;
                        default:
                            name = "subsdk" + (mod - 2);
                    }
                }
                mod++;
            }
            if (!name.equals("-")) {
                result.put(name, m.getAddress());
            }
        }
        return result;
    }

    public static ExpressionEvaluator expressionEvaluatorBuilder(Map<String, Long> regionMap, Debugger debugger) {
        return new ExpressionEvaluator(new ExpressionEvaluator.VariableProvider() {
            @Override
            public long get(String name) {
                return regionMap.get(name);
            }

            @Override
            public boolean containsVar(String value) {
                return regionMap.containsKey(value);
            }
        }, addr -> {
            if (!debugger.connected()) {
                return 0;
            }
            return debugger.peek64(addr);
        });
    }
}
