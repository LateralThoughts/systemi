package search

import java.io.Reader

import domain.Client
import org.apache.lucene.analysis.Analyzer.TokenStreamComponents
import org.apache.lucene.analysis.core.{LowerCaseFilter, StopFilter}
import org.apache.lucene.analysis.fr.{FrenchAnalyzer, FrenchLightStemFilter}
import org.apache.lucene.analysis.miscellaneous.SetKeywordMarkerFilter
import org.apache.lucene.analysis.ngram.EdgeNGramTokenFilter
import org.apache.lucene.analysis.standard.{StandardFilter, StandardTokenizer}
import org.apache.lucene.analysis.tokenattributes.{CharTermAttribute, OffsetAttribute}
import org.apache.lucene.analysis.util.{CharArraySet, ElisionFilter}
import org.apache.lucene.analysis.{Analyzer, TokenFilter}
import org.apache.lucene.document.{Document, Field, FieldType}
import org.apache.lucene.index._
import org.apache.lucene.search.{IndexSearcher, PhraseQuery}
import org.apache.lucene.store.RAMDirectory
import org.apache.lucene.util.Version
import resource._


trait SearchEngineFields {
  val ID_FIELD = "_id"
  val TEXT_FIELD = "text"
}

case class SimpleSearchEngine() extends ClientDefinitionIndexation with SearchEngineFields {
  val MAX_NUMBER_OF_DOCS = 50
  val directory = new RAMDirectory()
  val luceneVersion = Version.LUCENE_47

  val docAnalyzer = new LateralThoughtsClientAnalyzer(luceneVersion)

  def initWithDocuments(clients: List[Client]) {
    for (writer <- managed(openWriter)) {
      clients.map(client => writeDocument(writer, client))
    }
  }

  def addToIndex(client: Client) {
    for (writer <- managed(openWriter)) {
      writeDocument(writer, client)
    }
  }

  def update(id: String, client: Client) {
    for (writer <- managed(openWriter)) {
      writer.deleteDocuments(new Term(ID_FIELD, id.toString))
      writeDocument(writer, client)
    }
  }

  def createSearchQuery(q: String) = {
    val query = new PhraseQuery()
    query.setSlop(10)

    for (tokenStream <- managed(docAnalyzer.tokenStream("text", q))) {
      val offsetAttribute = tokenStream.addAttribute(classOf[OffsetAttribute])
      val charTermAttribute = tokenStream.addAttribute(classOf[CharTermAttribute])

      tokenStream.reset()
      while (tokenStream.incrementToken()) {
        val startOffset = offsetAttribute.startOffset()
        val endOffset = offsetAttribute.endOffset()

        val term = charTermAttribute.toString
        query.add(new Term("text", term))
      }
    }

    query
  }

  def search(q: String): List[String] = {

    val searchQuery = createSearchQuery(q)
    managed(DirectoryReader.open(directory)) acquireAndGet { dirReader =>
      val searcher = new IndexSearcher(dirReader)
      val results = searcher.search(searchQuery, MAX_NUMBER_OF_DOCS)

      results.scoreDocs.map(
        resultDoc => searcher.doc(resultDoc.doc).get(ID_FIELD)
      ).toList
    }
  }

  private def writeDocument(writer: IndexWriter, client: Client) {
    createDocFromClient(client) map (writer.addDocument(_))
  }

  private def openWriter = {
    val config = new IndexWriterConfig(luceneVersion, docAnalyzer)
    new IndexWriter(directory, config)
  }
}

trait ClientDefinitionIndexation extends SearchEngineFields {

  def createFieldText(client: Client): IndexableField = {
    val text = s"${client.name}\n ${client.address}\n ${client.city}\n ${client.postalCode}"
    val textIndexType = new FieldType()
    textIndexType.setIndexed(true)
    textIndexType.setStored(false)
    textIndexType.setTokenized(true)
    new Field("text", text, textIndexType)
  }

  def createFieldStored(fieldName: String, value: String) = {
    val textIndexType = new FieldType()
    textIndexType.setIndexed(false)
    textIndexType.setStored(true)
    textIndexType.setTokenized(false)
    new Field(fieldName, value, textIndexType)
  }

  def createFieldStoredAndIndexed(fieldName: String, value: String) = {
    val textIndexType = new FieldType()
    textIndexType.setIndexed(true)
    textIndexType.setStored(true)
    textIndexType.setTokenized(false)
    new Field(fieldName, value, textIndexType)
  }

  protected def createDocFromClient(client: Client): Option[Document] = {
    val document = new Document()
    document.add(createFieldStoredAndIndexed(ID_FIELD, client._id.stringify))
    document.add(createFieldStored("name", client.name))
    document.add(createFieldText(client))
    Some(document)
  }
}

case class LateralThoughtsClientAnalyzer(luceneVersion: Version) extends Analyzer {
  private val excludable = CharArraySet.EMPTY_SET

  override def createComponents(fieldName: String, reader: Reader) = {
    val source = new StandardTokenizer(luceneVersion, reader)
    var result: TokenFilter = new StandardFilter(luceneVersion, source)
    result = new ElisionFilter(result, FrenchAnalyzer.DEFAULT_ARTICLES)
    result = new LowerCaseFilter(luceneVersion, result)
    result = new StopFilter(luceneVersion, result, FrenchAnalyzer.getDefaultStopSet)

    if (!excludable.isEmpty)
      result = new SetKeywordMarkerFilter(result, excludable)

    result = new FrenchLightStemFilter(result)
    result = new EdgeNGramTokenFilter(luceneVersion, result, 2, 15)
    new TokenStreamComponents(source, result)
  }

}