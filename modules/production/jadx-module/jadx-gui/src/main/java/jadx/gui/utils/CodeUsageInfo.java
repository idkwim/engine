package jadx.gui.utils;

import jadx.api.CodePosition;
import jadx.api.JavaClass;
import jadx.api.JavaNode;
import jadx.gui.treemodel.CodeNode;
import jadx.gui.treemodel.JNode;
import jadx.gui.utils.search.StringRef;

public class CodeUsageInfo {

    private final JNodeCache nodeCache;
    private final Map<JNode, UsageInfo> usageMap = new HashMap<JNode, UsageInfo>();

    public CodeUsageInfo(JNodeCache nodeCache) {
        this.nodeCache = nodeCache;
    }

    public void processClass(JavaClass javaClass, CodeLinesInfo linesInfo, List<StringRef> lines) {
        Map<CodePosition, JavaNode> usage = javaClass.getUsageMap();
        for (Map.Entry<CodePosition, JavaNode> entry : usage.entrySet()) {
            CodePosition codePosition = entry.getKey();
            JavaNode javaNode = entry.getValue();
            addUsage(nodeCache.makeFrom(javaNode), javaClass, linesInfo, codePosition, lines);
        }
    }

    private void addUsage(JNode jNode, JavaClass javaClass,
                          CodeLinesInfo linesInfo, CodePosition codePosition, List<StringRef> lines) {
        UsageInfo usageInfo = usageMap.get(jNode);
        if (usageInfo == null) {
            usageInfo = new UsageInfo();
            usageMap.put(jNode, usageInfo);
        }
        int line = codePosition.getLine();
        JavaNode javaNodeByLine = linesInfo.getJavaNodeByLine(line);
        StringRef codeLine = lines.get(line - 1);
        JNode node = nodeCache.makeFrom(javaNodeByLine == null ? javaClass : javaNodeByLine);
        CodeNode codeNode = new CodeNode(node, line, codeLine);
        usageInfo.getUsageList().add(codeNode);
    }

    public List<CodeNode> getUsageList(JNode node) {
        UsageInfo usageInfo = usageMap.get(node);
        if (usageInfo == null) {
            return Collections.emptyList();
        }
        return usageInfo.getUsageList();
    }

    public static class UsageInfo {
        private final List<CodeNode> usageList = new ArrayList<CodeNode>();

        public List<CodeNode> getUsageList() {
            return usageList;
        }
    }
}