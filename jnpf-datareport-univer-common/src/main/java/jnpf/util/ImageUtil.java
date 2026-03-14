package jnpf.util;

import cn.hutool.core.util.RandomUtil;
import com.google.common.collect.ImmutableList;
import jnpf.enums.ImageEnum;
import jnpf.univer.data.resource.UniverDrawing;
import jnpf.univer.data.resource.UniverTransform;
import jnpf.univer.properties.*;
import jnpf.univer.zxing.UniverZxingModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImageUtil {

    public static UniverProperties zxing(UniverZxingModel zxingModel) {
        String base64 = ZxingUtil.zxing(zxingModel);
        return properties(base64, zxingModel.getUniverWidth(), zxingModel.getUniverHeight());
    }

    private static UniverProperties properties(String base64, int width, int height) {
        String drawingId = RandomUtil.randomString(10);
        UniverProperties properties = new UniverProperties();
        properties.setId("d");
        documentStyle(properties, width, height);
        body(properties, drawingId);
        drawings(properties, drawingId, base64, width, height);
        List<String> drawingsOrder = ImmutableList.of(drawingId);
        properties.setDrawingsOrder(drawingsOrder);
        return properties;
    }

    private static void documentStyle(UniverProperties properties, int width, int height) {
        UniverDocumentStyle documentStyle = new UniverDocumentStyle();
        UniverDocumentStyleConfig pageSize = new UniverDocumentStyleConfig();
        pageSize.setWidth(width);
        pageSize.setHeight(height);
        documentStyle.setPageSize(pageSize);
        documentStyle.setMarginTop(0);
        documentStyle.setMarginBottom(2);
        documentStyle.setMarginRight(2);
        documentStyle.setMarginLeft(2);

        UniverDocumentStyleConfig renderConfig = new UniverDocumentStyleConfig();
        renderConfig.setHorizontalAlign(0);
        renderConfig.setVerticalAlign(0);
        renderConfig.setCenterAngle(0);
        renderConfig.setVertexAngle(0);
        renderConfig.setWrapStrategy(0);
        renderConfig.setZeroWidthParagraphBreak(1);
        documentStyle.setRenderConfig(renderConfig);
        properties.setDocumentStyle(documentStyle);
    }

    private static void body(UniverProperties properties, String drawingId) {
        UniverBody body = new UniverBody();
        body.setDataStream("\b\r\n");
        body.setTextRuns(new ArrayList<>());
        List<UniverBodyConfig> paragraphs = new ArrayList<>();
        UniverBodyConfig config = new UniverBodyConfig();
        config.setStartIndex(1);
        UniverBodyConfig paragraphStyle = new UniverBodyConfig();
        paragraphStyle.setHorizontalAlign(0);
        config.setParagraphStyle(paragraphStyle);
        paragraphs.add(config);
        body.setParagraphs(paragraphs);

        List<UniverBodyConfig> sectionBreaks = new ArrayList<>();
        UniverBodyConfig section = new UniverBodyConfig();
        section.setStartIndex(2);
        sectionBreaks.add(section);
        body.setSectionBreaks(sectionBreaks);

        List<UniverBodyConfig> customBlocks = new ArrayList<>();
        UniverBodyConfig custom = new UniverBodyConfig();
        custom.setStartIndex(0);
        custom.setBlockId(drawingId);
        customBlocks.add(custom);
        body.setCustomBlocks(customBlocks);
        body.setCustomRanges(new ArrayList<>());
        body.setCustomDecorations(new ArrayList<>());

        properties.setBody(body);
    }

    private static void drawings(UniverProperties properties, String drawingId, String base64, int width, int height) {
        UniverDrawing drawing = new UniverDrawing();
        drawing.setUnitId("d");
        drawing.setSubUnitId("d");
        drawing.setDrawingId(drawingId);
        drawing.setDrawingType(0);
        drawing.setImageSourceType(ImageEnum.BASE64.name());
        drawing.setSource(base64);

        UniverTransform transform = new UniverTransform();
        transform.setLeft(0);
        transform.setTop(0);
        transform.setWidth(width);
        transform.setHeight(height);
        drawing.setTransform(transform);

        UniverTransform docTransform = new UniverTransform();
        UniverTransform size = new UniverTransform();
        size.setWidth(width);
        size.setHeight(height);
        docTransform.setSize(size);
        UniverTransform positionH = new UniverTransform();
        positionH.setRelativeFrom(0);
        positionH.setPosOffset(0);
        docTransform.setPositionH(positionH);
        UniverTransform positionV = new UniverTransform();
        positionV.setRelativeFrom(1);
        positionV.setPosOffset(0);
        docTransform.setPositionV(positionV);
        docTransform.setAngle(0);
        drawing.setDocTransform(docTransform);

        drawing.setBehindDoc(0);
        drawing.setTitle("");
        drawing.setDescription("");
        drawing.setLayoutType(0);
        drawing.setWrapText(0);
        drawing.setDistB(0);
        drawing.setDistL(0);
        drawing.setDistR(0);
        drawing.setDistT(0);

        Map<String, UniverDrawing> drawings = new HashMap<>();
        drawings.put(drawingId, drawing);
        properties.setDrawings(drawings);
    }
}
