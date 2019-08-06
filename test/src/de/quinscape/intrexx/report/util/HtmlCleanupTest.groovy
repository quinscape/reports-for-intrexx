package de.quinscape.intrexx.report.util;

class HtmlCleanupTest
{

  static void main(args)
  {
    def text = new File("test/src/de/quinscape/intrexx/report/util/htmlCleanUp.txt").text
    String result
    def start = new Date().time
    result = HtmlCleanup.cleanup(text, [
      "strong",
      "em",
      "br",
      "u",
      "p",
      "div"
    ])
    def dauer = new Date().time - start

    def start2 = new Date().time
    result = HtmlCleanup.cleanup(text)
    def dauer2 = new Date().time - start2
    println(result)

    println('cleanup("strong","em","br","u","p","div"): ' + dauer)
    println("cleanup: " + dauer2)
    def start3 = new Date().time
    result = HtmlCleanup.cleanupEntities(text)
    def dauer3 = new Date().time - start3
    println("cleanupEntities: " + dauer3)
  }
}
