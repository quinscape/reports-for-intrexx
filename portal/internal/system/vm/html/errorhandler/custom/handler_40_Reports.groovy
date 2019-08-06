/*
 * Error handler for "Intrexx for Reports"-Exceptions.
 */

def e = g_exception.getCauseOfType("de.quinscape.intrexx.report.IntrexxReportException")

if (e)
{
  switch (e.errorCode) {
    case "NOT_FOUND":
      title        = "Unbekannter Bericht"
      description = e.message
      description  = ""
      showEmbedded = false
      break
    case "AMBIGUOUS_IDENTIFIER":
      title        = "Nicht eindeutiger Bericht"
      description = e.message
      description  = ""
      showEmbedded = false
      break
    case "MISSING_MAIN_RESOURCE":
      title        = "Fehlende Hauptressource im Bericht"
      description = e.message
      description  = ""
      showEmbedded = false
      break
    case "NO_PERMISSION":
      title        = "Keine Berechtigungen für diesen Bericht"
      description = e.message
      description  = ""
      showEmbedded = false
      break
  }
}