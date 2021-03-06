package au.com.mountainpass.ryvr.rest;

import org.apache.http.Header;

public class LinkHeader {

  // credit: https://gist.github.com/eugenp/8269915
  public static String extractUriByRel(final Header[] headers, final String rel) {
    String uriWithSpecifiedRel = null;
    String linkRelation = null;
    for (final Header header : headers) {
      String headerValue = header.getValue();
      final int positionOfSeparator = headerValue.indexOf(';');
      String actualRel = extractRel(headerValue);
      if (actualRel.equals(rel)) {
        uriWithSpecifiedRel = headerValue.substring(1, positionOfSeparator - 1);
        break;
      }
    }

    return uriWithSpecifiedRel;
  }

  public static String extractRel(String headerValue) {
    final int positionOfSeparator = headerValue.indexOf(';');
    String linkRelation;
    linkRelation = headerValue.substring(positionOfSeparator + 1, headerValue.length()).trim();
    final int positionOfSeparator2 = linkRelation.indexOf(';');
    linkRelation = linkRelation.substring(0, positionOfSeparator2).trim();

    String actualRel = LinkHeader.extractTypeOfRelation(linkRelation);
    return actualRel;
  }

  private static String extractTypeOfRelation(final String linkRelation) {
    final int positionOfEquals = linkRelation.indexOf('=');
    return linkRelation.substring(positionOfEquals + 2, linkRelation.length() - 1).trim();
  }

  public static String extractTitle(Header header) {
    String headerValue = header.getValue();
    final int positionOfSeparator = headerValue.indexOf(';');
    String properties;
    properties = headerValue.substring(positionOfSeparator + 1, headerValue.length()).trim();
    final int positionOfSeparator2 = properties.indexOf(';');
    String titleProperties = properties.substring(positionOfSeparator2 + 1, properties.length())
        .trim();
    return titleProperties.substring(titleProperties.indexOf('=') + 2, titleProperties.length() - 1)
        .trim();
  }

  public static String extractUri(Header header) {
    String headerValue = header.getValue();
    final int positionOfSeparator = headerValue.indexOf(';');
    return headerValue.substring(1, positionOfSeparator - 1);
  }

}
