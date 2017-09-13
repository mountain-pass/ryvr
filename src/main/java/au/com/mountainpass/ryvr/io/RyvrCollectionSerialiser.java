package au.com.mountainpass.ryvr.io;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import au.com.mountainpass.ryvr.model.Record;
import au.com.mountainpass.ryvr.model.Ryvr;
import io.undertow.servlet.spec.ServletOutputStreamImpl;

@Component
public class RyvrCollectionSerialiser {
  private static final int outputBufferSize = 8192 * 1024;
  public final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

  public void toJson(Ryvr ryvr, long page, OutputStream o) throws IOException {
    toJsonWithWriter(ryvr, page, o);
  }

  private StringBuilderWriter writer = new StringBuilderWriter(outputBufferSize);

  private static final char[] titlePre = "{\"title\":\"".toCharArray();
  private static final char[] pagePre = "\",\"page\":".toCharArray();
  private static final char[] pageSizePre = ",\"pageSize\":".toCharArray();
  private static final char[] rowsPre = ",\"rows\":[".toCharArray();
  private static final char[] comma = ",".toCharArray();
  private static char[] openArray = "[".toCharArray();
  private static final char[] quote = "\"".toCharArray();
  private static final char[] closeArrays = "]".toCharArray();
  private static final char[] columnsPre = "],\"columns\":[".toCharArray();
  private static final char[] closeObjects = "}".toCharArray();

  public void toJsonWithWriter(Ryvr ryvr, long page, OutputStream o) throws IOException {
    // baos.reset();
    writer.getBuilder().setLength(0);
    o.flush();
    // if (page <= 0) {
    // page = ryvr.getPages();
    // }
    // Writer writer = new BufferedWriter(w, outputBufferSize);
    Iterator<Record> iterator = ryvr.getSource().iterator((page - 1) * ryvr.getPageSize());
    writer.write(titlePre, 0, 10);
    writer.write(ryvr.getTitle());
    writer.write(pagePre, 0, 9);
    writer.write(Long.toString(page));
    writer.write(pageSizePre, 0, 12);
    writer.write(Integer.toString(ryvr.getPageSize()));
    writer.write(rowsPre, 0, 9);
    for (int i = 0, pageSize = ryvr.getPageSize(); i < pageSize; ++i) {
      try {
        Record record = iterator.next();
        if (i != 0) {
          writer.write(comma, 0, 1);
        }
        writer.write(openArray, 0, 1);
        for (int j = 0, size = record.size(); j < size; ++j) {
          if (j != 0) {
            writer.write(comma, 0, 1);
          }
          final Object value = record.getField(j).getValue();
          if (value instanceof String) {
            writer.write(quote, 0, 1);
            escapeAndWrite((String) value);
            writer.write(quote, 0, 1);
            // } else if (value instanceof Timestamp) {
            // Timestamp ts = (Timestamp) value;
            // writer.write(Long.toString(ts.getTime()));
          } else {
            writer.write(value.toString());
          }
        }
        writer.write(closeArrays, 0, 1);
      } catch (NoSuchElementException e) {
        break;
      }
    }
    writer.write(columnsPre, 0, 13);
    String[] fieldNames = ryvr.getFieldNames();
    if (fieldNames != null) {
      for (int i = 0, size = fieldNames.length; i < size; ++i) {
        if (i != 0) {
          writer.write(comma, 0, 1);
        }
        writer.write(quote, 0, 1);
        escapeAndWrite(fieldNames[i]);
        writer.write(quote, 0, 1);
      }
    }
    writer.write(closeArrays, 0, 1);
    writer.write(closeObjects, 0, 1);
    o.write(writer.getBuilder().toString().getBytes());
    if (o instanceof ServletOutputStreamImpl) {
      ServletOutputStreamImpl sosi = (ServletOutputStreamImpl) o;
      sosi.closeAsync();
    } else {
      o.flush();
    }
  }

  private final static LRUMap escaped = new LRUMap(8192);

  private void escapeAndWrite(final String value) throws IOException {
    final String e = (String) escaped.get(value);
    if (e == null) {
      String e2 = StringEscapeUtils.escapeJson(value);
      escaped.put(value, e2);
      writer.write(e2);
    } else {
      writer.write(e);
    }
  }

}
