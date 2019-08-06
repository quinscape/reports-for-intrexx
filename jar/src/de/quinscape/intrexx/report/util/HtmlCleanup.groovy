package de.quinscape.intrexx.report.util;

import java.util.regex.*
import java.util.regex.Pattern.*

import org.apache.commons.lang.*


class HtmlCleanup
{

  /**
   * Entfernt alle HTML-Tags außer <strong>, <em>, <u> und <br/>.
   * <b> und <i> werden in <strong> bzw. <em> umgewandelt. schließende </p> und
   * </div> werden in </br> umgewandelt. Alle HTML-Entities ausser &amp;, &lt;,
   * &gt; und &nbsp; werden aufgelöst; 
   */
  static def String cleanup(String text)
  {
    String cleaned = cleanupHighlight(text)
    cleaned = cleanupBr(cleaned)
    cleaned = cleanup(cleaned, ["strong", "em", "u", "br"])
    cleaned = cleanupEntities(cleaned)
    return cleaned
  }

  static def String cleanupHighlight(String html)
  {
    return html
    // (?i)       -> ignore case
    // (\w+:)?    -> möglicher Namespace
    // (\s[^>]*)? -> Whitespace und beliebige Zeichen vor den Tag-Ende
    .replaceAll( /(?i)<(\w+:)?(b|strong)(\s[^>]*)?>/   , "<strong>")
    .replaceAll( /(?i)<\/(\w+:)?(b|strong)(\s[^>]*)?>/ , "</strong>")
    .replaceAll( /(?i)<(\w+:)?(i|em)(\s[^>]*)?>/       , "<em>")
    .replaceAll( /(?i)<\/(\w+:)?(i|em)(\s[^>]*)?>/     , "</em>")
  }

  /**
   * Ersetzt schließende bzw. geschlossene p- und div-Tags durch <br/>. Öffnende Tags entfallen.
   */
  static def String cleanupBr(String html)
  {
    return html
    .replaceAll( /(?i)<(\w+:)?(p|div)(\/|\s[^>]*)?>/   , "")
    .replaceAll( /(?i)<(\w+:)?(p|div)(\s*)?\/>/        , "<br/>")
    .replaceAll( /(?i)<\/(\w+:)?(p|div)(\s[^>]*)?>/    , "<br/>")
  }

  /**
   * Entfernt alle HTML-Tags außer den angegebenen. Die angegeben Tags werden von
   * allen Attributen gesäubert.
   */
  static def String cleanup(String html, List<String> validTags)
  {
    def valid = validTags.join("|")
    def cleaned = html
        // (?i)    -> ignore case
        // (\w+:)? -> möglicher Namespace
        // [\s\/>] -> Whitespace oder Tag-Ende
        .replaceAll( /(?i)<(?!\/)(\w+:)?(?!(${valid})[\s\/>])[^>]*>/ , "")
        .replaceAll( /(?i)<\/(\w+:)?(?!(${valid})[\s>])[^>]*>/       , "")
        .replaceAll( /(?i)<(?:\w+:)?(${valid})(?:(\/?>)|\s[^\/>]*>)/ , {all, tag, end ->
          "<" + tag.toLowerCase() + (end ? end : ">") })
        .replaceAll( /(?i)<\/(?:\w+:)?(${valid})(?:\s[^\/>]*)?>/     , {all, tag  ->
          "</" + tag.toLowerCase() + ">" })

    return cleaned
  }

  /**
   * Löst alle HTML-Entities auf, ausser &amp;, &lt;, &gt; in allen
   * möglichen Schreibweisen. 
   */
  static String cleanupEntities(String html)
  {
    // Statisch vorkompilierte Pattern bringen keinen Performance-Unterschied
    String cleaned = html
        // Ausgewählte Entities vor der Auflösung bewahren
        // (?i) -> ignore case
        .replaceAll( /(?i)&(amp|#38|#x0026);/   , "&amp;amp;")
        .replaceAll( /(?i)&(lt|#60|#x003C);/    , "&amp;lt;")
        .replaceAll( /(?i)&(gt|#62|#x003E);/    , "&amp;gt;")
    //.replaceAll( /(?i)&(nbsp|#160|#x00A0);/ , "&amp;nbsp;")

    return StringEscapeUtils.unescapeHtml(cleaned.toString());
  }

}

