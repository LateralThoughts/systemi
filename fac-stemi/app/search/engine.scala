package search

import org.apache.lucene.store.RAMDirectory
import org.apache.lucene.index._
import org.apache.lucene.util.Version
import org.apache.lucene.analysis.fr.{FrenchLightStemFilter, FrenchAnalyzer}
import org.apache.lucene.document.{FieldType, Field, Document}
import org.apache.lucene.search.{IndexSearcher, PhraseQuery}
import org.apache.lucene.analysis.tokenattributes.{CharTermAttribute, OffsetAttribute}
import domain.Client
import org.apache.lucene.analysis.{TokenFilter, Analyzer}
import java.io.Reader
import org.apache.lucene.analysis.standard.{StandardTokenizer, StandardFilter}
import org.apache.lucene.analysis.util.{ElisionFilter, CharArraySet}
import org.apache.lucene.analysis.core.{StopFilter, LowerCaseFilter}
import org.apache.lucene.analysis.miscellaneous.SetKeywordMarkerFilter
import org.apache.lucene.analysis.Analyzer.TokenStreamComponents
import org.apache.lucene.analysis.ngram.EdgeNGramTokenFilter


trait SearchEngineFields {
  val ID_FIELD = "_id"
  val TEXT_FIELD = "text"
}

case class SimpleSearchEngine() extends ClientDefinitionIndexation with SearchEngineFields{
  val MAX_NUMBER_OF_DOCS = 50
  val directory = new RAMDirectory()
  val luceneVersion = Version.LUCENE_47

  val docAnalyzer = new LateralThoughtsClientAnalyzer(luceneVersion)

  def initWithDocuments(clients : List[Client]) {
    val writer = openWriter
    clients.map( client => writeDocument(writer, client))
    writer.close(true)
  }

  def addToIndex(client : Client) {
    val writer = openWriter
    writeDocument(writer, client)
    writer.close(true)
  }

  def update(id: String, client : Client) {
    val writer = openWriter
    writer.deleteDocuments(new Term(ID_FIELD, id.toString))
    writeDocument(writer, client)
    writer.close(true)
  }

  def createSearchQuery(q: String) = {
    val query = new PhraseQuery()
    query.setSlop(10)

    val tokenStream = docAnalyzer.tokenStream("text", q)
    val offsetAttribute = tokenStream.addAttribute(classOf[OffsetAttribute])
    val charTermAttribute = tokenStream.addAttribute(classOf[CharTermAttribute])

    tokenStream.reset()
    while (tokenStream.incrementToken()) {
      val startOffset = offsetAttribute.startOffset()
      val endOffset = offsetAttribute.endOffset()

      val term = charTermAttribute.toString
      query.add(new Term("text", term))
    }
    tokenStream.close()

    query
  }

  def search(q: String) : List[String] = {

    val searchQuery = createSearchQuery(q)
    val dirReader = DirectoryReader.open(directory)
    val searcher = new IndexSearcher(dirReader)
    val results = searcher.search(searchQuery, MAX_NUMBER_OF_DOCS)

    val resultClients = results.scoreDocs.map(
      resultDoc => searcher.doc(resultDoc.doc).get(ID_FIELD)
    ).toList

    dirReader.close()
    resultClients
  }


  /**
   * Close directory after search is done
   */
  def close() { directory.close() }

  private def writeDocument(writer: IndexWriter, client: Client) {
    createDocFromClient(client) map ( writer.addDocument(_) )
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

  protected def createDocFromClient(client: Client) : Option[Document] = {
    client._id match  {
      case Some(id) =>
        val document = new Document()
        document.add(createFieldStoredAndIndexed(ID_FIELD, id.stringify))
        document.add(createFieldStored("name", client.name))
        document.add(createFieldText(client))
        Some(document)

      case None => None
    }

  }
}

case class LateralThoughtsClientAnalyzer(luceneVersion : Version) extends Analyzer {
  private val excludable = CharArraySet.EMPTY_SET

  override def createComponents(fieldName : String, reader: Reader) = {
    val source = new StandardTokenizer(luceneVersion, reader)
    var result : TokenFilter = new StandardFilter(luceneVersion, source)
    result = new ElisionFilter(result, FrenchAnalyzer.DEFAULT_ARTICLES)
    result = new LowerCaseFilter(luceneVersion, result)
    result = new StopFilter(luceneVersion, result, FrenchAnalyzer.getDefaultStopSet)

    if(!excludable.isEmpty)
      result = new SetKeywordMarkerFilter(result, excludable)

    result = new FrenchLightStemFilter(result)
    result = new EdgeNGramTokenFilter(luceneVersion, result, 2, 15)
    new TokenStreamComponents(source, result)
  }

}