package scribe.writer

import java.io.PrintStream

import scribe.output._
import scribe.{ANSI, Level, LogRecord, Logger}

object ANSIConsoleWriter extends Writer {
  private object ansi {
    var fg: Option[ANSI] = None
    var bg: Option[ANSI] = None
    var bold: Boolean = false
    var italic: Boolean = false
    var underline: Boolean = false
    var strikethrough: Boolean = false
  }

  override def write[M](record: LogRecord[M], output: LogOutput): Unit = {
    val stream = if (record.level <= Level.Info) {
      Logger.system.out
    } else {
      Logger.system.err
    }
    writeOutput(output, stream)
    stream.println()
  }

  private def writeOutput(output: LogOutput, stream: PrintStream): Unit = output match {
    case o: TextOutput => stream.print(o.plainText)
    case o: CompositeOutput => o.entries.foreach(writeOutput(_, stream))
    case o: ColoredOutput => {
      val color = color2fg(o.color)
      stream.print(color.ansi)
      val previous = ansi.fg
      ansi.fg = Some(color)
      try {
        writeOutput(o.output, stream)
      } finally {
        ansi.fg = previous
        reset(stream)
      }
    }
    case o: BackgroundColoredOutput => {
      val color = color2bg(o.color)
      stream.print(color.ansi)
      val previous = ansi.bg
      ansi.bg = Some(color)
      try {
        writeOutput(o.output, stream)
      } finally {
        ansi.bg = previous
        reset(stream)
      }
    }
    case o: URLOutput => {
      stream.print("""\u001b]8;;""")
      stream.print(o.url)
      stream.print("""\u001b\""")
      if (o.output == EmptyOutput) {
        stream.print(o.url)
      } else {
        writeOutput(o.output, stream)
      }
      stream.print("""\u001b]8;;\u001b\""")
    }
    case o: BoldOutput => {
      val previous = ansi.bold
      ansi.bold = true
      try {
        stream.print(ANSI.fx.Bold.ansi)
        writeOutput(o.output, stream)
      } finally {
        ansi.bold = previous
        reset(stream)
      }
    }
    case o: ItalicOutput => {
      val previous = ansi.italic
      ansi.italic = true
      try {
        stream.print(ANSI.fx.Italic.ansi)
        writeOutput(o.output, stream)
      } finally {
        ansi.italic = previous
        reset(stream)
      }
    }
    case o: UnderlineOutput => {
      val previous = ansi.underline
      ansi.underline = true
      try {
        stream.print(ANSI.fx.Underline.ansi)
        writeOutput(o.output, stream)
      } finally {
        ansi.underline = previous
        reset(stream)
      }
    }
    case o: StrikethroughOutput => {
      val previous = ansi.strikethrough
      ansi.strikethrough = true
      try {
        stream.print(ANSI.fx.Strikethrough.ansi)
        writeOutput(o.output, stream)
      } finally {
        ansi.strikethrough = previous
        reset(stream)
      }
    }
    case _ => stream.print(output.plainText)      // TODO: support warning unsupported
  }

  private def reset(stream: PrintStream): Unit = {
    stream.print(ANSI.ctrl.Reset)
    ansi.fg.map(_.ansi).foreach(stream.print)
    ansi.bg.map(_.ansi).foreach(stream.print)
    if (ansi.bold) stream.print(ANSI.fx.Bold.ansi)
    if (ansi.italic) stream.print(ANSI.fx.Italic.ansi)
    if (ansi.underline) stream.print(ANSI.fx.Underline.ansi)
    if (ansi.strikethrough) stream.print(ANSI.fx.Strikethrough.ansi)
  }

  private def color2fg(color: Color): ANSI = color match {
    case Color.Black => ANSI.fg.Black
    case Color.Blue => ANSI.fg.Blue
    case Color.Cyan => ANSI.fg.Cyan
    case Color.Green => ANSI.fg.Green
    case Color.Magenta => ANSI.fg.Magenta
    case Color.Red => ANSI.fg.Red
    case Color.White => ANSI.fg.White
    case Color.Yellow => ANSI.fg.Yellow
    case Color.Gray => ANSI.fg.Gray
    case Color.BrightBlue => ANSI.fg.BrightBlue
    case Color.BrightCyan => ANSI.fg.BrightCyan
    case Color.BrightGreen => ANSI.fg.BrightGreen
    case Color.BrightMagenta => ANSI.fg.BrightMagenta
    case Color.BrightRed => ANSI.fg.BrightRed
    case Color.BrightWhite => ANSI.fg.BrightWhite
    case Color.BrightYellow => ANSI.fg.BrightYellow
  }

  private def color2bg(color: Color): ANSI = color match {
    case Color.Black => ANSI.bg.Black
    case Color.Blue => ANSI.bg.Blue
    case Color.Cyan => ANSI.bg.Cyan
    case Color.Green => ANSI.bg.Green
    case Color.Magenta => ANSI.bg.Magenta
    case Color.Red => ANSI.bg.Red
    case Color.White => ANSI.bg.White
    case Color.Yellow => ANSI.bg.Yellow
    case Color.Gray => ANSI.bg.Gray
    case Color.BrightBlue => ANSI.bg.BrightBlue
    case Color.BrightCyan => ANSI.bg.BrightCyan
    case Color.BrightGreen => ANSI.bg.BrightGreen
    case Color.BrightMagenta => ANSI.bg.BrightMagenta
    case Color.BrightRed => ANSI.bg.BrightRed
    case Color.BrightWhite => ANSI.bg.BrightWhite
    case Color.BrightYellow => ANSI.bg.BrightYellow
  }
}