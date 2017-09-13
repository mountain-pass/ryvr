package au.com.mountainpass.ryvr.rest;

import org.apache.http.Header;

public class LinkHeader {

  // credit: https://gist.github.com/eugenp/8269915
  public static String extractURIByRel(final Header[] headers, final String rel) {
    String uriWithSpecifiedRel = null;
    String linkRelation = null;
    for (final Header header : headers) {
      String headerValue = header.getValue();
      final int positionOfSeparator = headerValue.indexOf(';');
      linkRelation = headerValue.substring(positionOfSeparator + 1, headerValue.length()).trim();
      final int positionOfSeparator2 = linkRelation.indexOf(';');
      linkRelation = linkRelation.substring(0, positionOfSeparator2).trim();

      if (LinkHeader.extractTypeOfRelation(linkRelation).equals(rel)) {
        uriWithSpecifiedRel = headerValue.substring(1, positionOfSeparator - 1);
        break;
      }
    }

    return uriWithSpecifiedRel;
  }

  static String extractTypeOfRelation(final String linkRelation) {
    final int positionOfEquals = linkRelation.indexOf('=');
    return linkRelation.substring(positionOfEquals + 2, linkRelation.length() - 1).trim();
  }

}
