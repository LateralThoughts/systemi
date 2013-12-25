package util.pdf

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;

import org.w3c.dom.Document;
import org.w3c.tidy.Tidy;
import org.xhtmlrenderer.pdf.ITextFSImage;
import org.xhtmlrenderer.pdf.ITextFontResolver;
import org.xhtmlrenderer.pdf.ITextOutputDevice;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xhtmlrenderer.pdf.ITextUserAgent;
import org.xhtmlrenderer.resource.CSSResource;
import org.xhtmlrenderer.resource.ImageResource;
import org.xhtmlrenderer.resource.XMLResource;

import play.Logger;
import play.api.Play;
import play.api.templates.Html;
import play.mvc.Result;
import play.mvc.Results;
import scala.Option;

import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.BaseFont;

object PDF {

        private def PLAY_DEFAULT_URL = "http://invoice.lateral-thoughts.com";

        class MyUserAgent(outputDevice : ITextOutputDevice) extends ITextUserAgent(outputDevice) {

                override def getImageResource(uri: String) = {
                        val option = Play.current.resourceAsStream(uri)
                        if (option.isDefined) {
                                val stream = option.get;
                                try {
                                        val image = Image.getInstance(getData(stream))
                                        scaleToOutputResolution(image)
                                        new ImageResource("test", new ITextFSImage(image))
                                } catch {
                                    case e: Exception => {
                                        Logger.error("fetching image " + uri, e)
                                        throw new RuntimeException(e)
                                    }
                                }
                        } else {
                                super.getImageResource(uri)
                        }
                }

                override def getCSSResource(uri: String) = {
                        try {
                                // uri is in fact a complete URL
                                val path = new URL(uri).getPath()
                                val option = Play.current.resourceAsStream(path)
                                if (option.isDefined) {
                                        new CSSResource(option.get);
                                } else {
                                        super.getCSSResource(uri);
                                }
                        } catch {
                            case e: MalformedURLException => {
                                Logger.error("fetching css " + uri, e);
                                throw new RuntimeException(e);
                            }
                        }
                }

                override def getBinaryResource(uri: String) = {
                        val option = Play.current.resourceAsStream(uri)
                        if (option.isDefined) {
                                val stream = option.get
                                val baos = new ByteArrayOutputStream()
                                try {
                                        copy(stream, baos);
                                } catch {
                                    case e: IOException => {
                                        Logger.error("fetching binary " + uri, e)
                                        throw new RuntimeException(e)
                                    }
                                }
                                baos.toByteArray();
                        } else {
                                super.getBinaryResource(uri);
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
                        val factor = getSharedContext().getDotsPerPixel()
                        image.scaleAbsolute(image.getPlainWidth() * factor, image.getPlainHeight() * factor)
                }

                private def getData(stream: InputStream) = {
                        val baos = new ByteArrayOutputStream()
                        copy(stream, baos)
                        baos.toByteArray()
                }

                private def copy(stream: InputStream, os: OutputStream) {
                        val buffer = new Array[Byte](1024)
                        var outCondition = true
                        while (outCondition) {
                                val len = stream.read(buffer);
                                os.write(buffer, 0, len);
                                if (len < buffer.length)
                                        outCondition = false
                        }
                }
        }

        def ok(html: Html) = {
                val pdf = toBytes(tidify(html.body))
                Results.ok(pdf).as("application/pdf")
        }

        def toBytes(html: Html) : Array[Byte] = toBytes(tidify(html.body))

        def toBytes(string: String) : Array[Byte] = toBytes(string, PLAY_DEFAULT_URL)

        def tidify(body: String) = {
                val tidy = new Tidy()
                tidy.setXHTML(true)
                val writer = new StringWriter()
                tidy.parse(new StringReader(body), writer)
                writer.getBuffer().toString()
        }

        def toBytes(html: Html, documentBaseURL: String) : Array[Byte] = {
                val pdf : Array[Byte] = toBytes(tidify(html.body), documentBaseURL)
                pdf
        }

        def toBytes(string: String, documentBaseURL: String) : Array[Byte] = {
                val os = new ByteArrayOutputStream()
                toStream(string, os, documentBaseURL)
                os.toByteArray()
        }

        def toStream(string: String, os : OutputStream) {
                toStream(string, os, PLAY_DEFAULT_URL)
        }

        def toStream(string: String, os: OutputStream, documentBaseURL: String) = {
                try {
                        val reader = new StringReader(string)
                        val renderer = new ITextRenderer()
                        addFontDirectory(renderer.getFontResolver(), Play.current.path + "/conf/fonts")
                        val myUserAgent = new MyUserAgent(renderer.getOutputDevice)
                        myUserAgent.setSharedContext(renderer.getSharedContext())
                        renderer.getSharedContext().setUserAgentCallback(myUserAgent)
                        val document = XMLResource.load(reader).getDocument()
                        renderer.setDocument(document, documentBaseURL)
                        renderer.layout
                        renderer.createPDF(os)
                } catch {
                    case e: Exception => Logger.error("Creating document from template", e)
                }
        }

        private def addFontDirectory(fontResolver: ITextFontResolver,
                        directory : String ) = {
                val dir = new File(directory);
                for ( file : File <- dir.listFiles) {
                        fontResolver.addFont(file.getAbsolutePath(), BaseFont.IDENTITY_H,
                                        BaseFont.EMBEDDED)
                }
        }

}