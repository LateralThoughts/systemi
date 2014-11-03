package util.pdf

import java.awt.Color
import java.io.{ByteArrayOutputStream, File, IOException, InputStream, OutputStream, StringReader, StringWriter}
import java.net.{MalformedURLException, URL}

import com.lowagie.text.{Element, Image}
import com.lowagie.text.pdf.{PdfGState, PdfStamper, PdfReader, BaseFont}
import org.w3c.tidy.Tidy
import org.xhtmlrenderer.pdf.{ITextFSImage, ITextFontResolver, ITextOutputDevice, ITextRenderer, ITextUserAgent}
import org.xhtmlrenderer.resource.{CSSResource, ImageResource, XMLResource}
import play.Logger
import play.api.Play
import play.twirl.api.Html

object PDF {

        private val PLAY_DEFAULT_URL = Play.maybeApplication.flatMap(_.configuration.getString("application.hostname")).getOrElse("http://localhost:9000")

        class MyUserAgent(outputDevice : ITextOutputDevice) extends ITextUserAgent(outputDevice) {

                override def getImageResource(uri: String) = {
                        val option = Play.current.resourceAsStream(uri)
                        if (option.isDefined) {
                                val stream = option.get
                                try {
                                        val image = Image.getInstance(getData(stream))
                                        scaleToOutputResolution(image)
                                        new ImageResource("test", new ITextFSImage(image))
                                } catch {
                                    case e: Exception =>
                                      Logger.error("fetching image " + uri, e)
                                      throw new RuntimeException(e)
                                }
                        } else {
                                super.getImageResource(uri)
                        }
                }

                override def getCSSResource(uri: String) = {
                        try {
                                // uri is in fact a complete URL
                                val path = new URL(uri).getPath
                                val option = Play.current.resourceAsStream(path)
                                if (option.isDefined) {
                                        new CSSResource(option.get)
                                } else {
                                        super.getCSSResource(uri)
                                }
                        } catch {
                            case e: MalformedURLException =>
                              Logger.error("fetching css " + uri, e)
                              throw new RuntimeException(e)
                        }
                }

                override def getBinaryResource(uri: String) = {
                        val option = Play.current.resourceAsStream(uri)
                        if (option.isDefined) {
                                val stream = option.get
                                val baos = new ByteArrayOutputStream()
                                try {
                                        copy(stream, baos)
                                } catch {
                                    case e: IOException =>
                                      Logger.error("fetching binary " + uri, e)
                                      throw new RuntimeException(e)
                                }
                                baos.toByteArray
                        } else {
                                super.getBinaryResource(uri)
                        }
                }

                override def getXMLResource(uri: String) = {
                        val option = Play.current.resourceAsStream(uri)
                        if (option.isDefined) {
                                XMLResource.load(option.get)
                        } else {
                                super.getXMLResource(uri)
                        }
                }

                private def scaleToOutputResolution(image : Image) {
                        val factor = getSharedContext.getDotsPerPixel
                        image.scaleAbsolute(image.getPlainWidth * factor, image.getPlainHeight * factor)
                }

                private def getData(stream: InputStream) = {
                        val baos = new ByteArrayOutputStream()
                        copy(stream, baos)
                        baos.toByteArray
                }

                private def copy(stream: InputStream, os: OutputStream) {
                        val buffer = new Array[Byte](1024)
                        var outCondition = true
                        while (outCondition) {
                                val len = stream.read(buffer)
                                os.write(buffer, 0, len)
                                if (len < buffer.length)
                                        outCondition = false
                        }
                }
        }

        def toBytes(html: Html) : Array[Byte] = toBytes(tidify(html.body))

        def addWatermarkToPdf(pdfBytes: Array[Byte], watermark: String, color: Color) = {
          // Read the existing PDF document
          val pdfReader = new PdfReader(pdfBytes)
          val pdfOutputStream = new ByteArrayOutputStream()
          // Get the PdfStamper object
          val pdfStamper = new PdfStamper(pdfReader, pdfOutputStream)
          // Get the PdfContentByte type by pdfStamper.
          val underContent = pdfStamper.getUnderContent(1)

          val bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.EMBEDDED)
          val gs = new PdfGState()
          gs.setFillOpacity(0.8f)
          underContent.setGState(gs)
          underContent.beginText()
          underContent.setFontAndSize(bf, 35)
          underContent.setColorFill(color)
          underContent.showTextAligned(Element.ALIGN_CENTER, watermark, 300, 700, 45)
          underContent.endText()

          pdfStamper.close()

          pdfOutputStream.toByteArray
        }

        private def toBytes(string: String) : Array[Byte] = toBytes(string, PLAY_DEFAULT_URL)

        private def tidify(body: String) = {
                val tidy = new Tidy()
                tidy.setXHTML(true)
                val writer = new StringWriter()
                tidy.parse(new StringReader(body), writer)
                writer.getBuffer.toString
        }

        private def toBytes(string: String, documentBaseURL: String) : Array[Byte] = {
                val os = new ByteArrayOutputStream()
                transformToStream(string, os, documentBaseURL)
                os.toByteArray
        }

        private def transformToStream(string: String, os: OutputStream, documentBaseURL: String) = {
                try {
                        val reader = new StringReader(string)
                        val renderer = new ITextRenderer()
                        addFontDirectory(renderer.getFontResolver, Play.current.path + "/conf/fonts")
                        val myUserAgent = new MyUserAgent(renderer.getOutputDevice)
                        myUserAgent.setSharedContext(renderer.getSharedContext)
                        renderer.getSharedContext.setUserAgentCallback(myUserAgent)
                        val document = XMLResource.load(reader).getDocument
                        renderer.setDocument(document, documentBaseURL)
                        renderer.layout()
                        renderer.createPDF(os)
                } catch {
                    case e: Exception => Logger.error("Creating document from template", e)
                }
        }

        private def addFontDirectory(fontResolver: ITextFontResolver,
                        directory : String ) = {
                val dir = new File(directory)
                for ( file : File <- dir.listFiles) {
                        fontResolver.addFont(file.getAbsolutePath, BaseFont.IDENTITY_H,
                                        BaseFont.EMBEDDED)
                }
        }

}