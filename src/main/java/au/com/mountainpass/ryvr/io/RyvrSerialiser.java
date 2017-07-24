package au.com.mountainpass.ryvr.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import au.com.mountainpass.ryvr.model.Record;
import au.com.mountainpass.ryvr.model.Ryvr;

@Component
public class RyvrSerialiser {
  private static final int outputBufferSize = 8192 * 1024;
  public final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

  public void toJson(Ryvr ryvr, long page, OutputStream o) throws IOException {
    toJsonWithWriter(ryvr, page, o);
  }

  private ByteArrayOutputStream baos = new ByteArrayOutputStream(outputBufferSize);
  private Writer writer = new OutputStreamWriter(baos); // new BufferedWriter(new
  // // OutputStreamWriter(baos),
  // // outputBufferSize);

  public void toJsonWithWriter(Ryvr ryvr, long page, OutputStream o) throws IOException {
    baos.reset();
    // if (page <= 0) {
    // page = ryvr.getPages();
    // }
    // Writer writer = new BufferedWriter(w, outputBufferSize);
    Iterator<Record> iterator = ryvr.getSource().iterator((page - 1) * ryvr.getPageSize());
    writer.write("{\"title\":\"", 0, 10);
    writer.write(ryvr.getTitle());
    writer.write("\",\"page\":", 0, 9);
    writer.write(Long.toString(page));
    writer.write(",\"pageSize\":", 0, 12);
    writer.write(Integer.toString(ryvr.getPageSize()));
    writer.write(",\"rows\":[", 0, 9);

    for (int i = 0, pageSize = ryvr.getPageSize(); i < pageSize && iterator.hasNext(); ++i) {
      Record record = iterator.next();
      if (i != 0) {
        writer.write(',');
      }
      writer.write('[');
      for (int j = 0, size = record.size(); j < size; ++j) {
        if (j != 0) {
          writer.write(',');
        }
        final Object value = record.getField(j).getValue();
        if (value instanceof String) {
          writer.write('"');
          // todo: JSON encode
          writer.write((String) value);
          writer.write('"');
        } else {
          writer.write(value.toString());
        }
      }
      writer.write(']');
    }
    writer.write("],\"columns\":[", 0, 13);
    String[] fieldNames = ryvr.getFieldNames();
    if (fieldNames != null) {
      for (int i = 0, size = fieldNames.length; i < size; ++i) {
        if (i != 0) {
          writer.write(',');
        }
        writer.write('"');
        writer.write(fieldNames[i]);
        writer.write('"');
      }
    }
    writer.write(']');
    // if (page == ryvr.getPages()) {
    // writer.write(",\"count\":", 0, 9);
    // writer.write(Long.toString(ryvr.getCount()));
    // }
    writer.write('}');
    writer.flush();

    o.write(baos.toByteArray());
    o.flush();
  }

}
