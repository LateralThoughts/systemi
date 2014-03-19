package search.engine

import org.apache.lucene.store.RAMDirectory
import org.apache.lucene.index._
import org.apache.lucene.util.Version
import org.apache.lucene.analysis.fr.{FrenchLightStemFilter, FrenchAnalyzer}
import org.apache.lucene.document.{FieldType, Field, Document}
import org.apache.lucene.search.{IndexSearcher, PhraseQuery}
import org.apache.lucene.analysis.tokenattributes.{CharTermAttribute, OffsetAttribute}
import domain.ClientDefinition
import org.apache.lucene.analysis.{TokenFilter, Analyzer}
import java.io.Reader
import org.apache.lucene.analysis.standard.{StandardTokenizer, StandardFilter, StandardAnalyzer}
import org.apache.lucene.analysis.util.{ElisionFilter, CharArraySet}
import org.apache.lucene.analysis.core.{StopFilter, LowerCaseFilter}
import org.apache.lucene.analysis.miscellaneous.SetKeywordMarkerFilter
import org.apache.lucene.analysis.Analyzer.TokenStreamComponents
import org.apache.lucene.analysis.ngram.{EdgeNGramTokenFilter, EdgeNGramFilterFactory}


case class SimpleSearchEngine(clients : collection.mutable.Map[Int, ClientDefinition]) extends ClientDefinitionIndexation {
  val MAX_NUMBER_OF_DOCS = 50
  val directory = new RAMDirectory()
  val luceneVersion = Version.LUCENE_47
  val docAnalyzer = new LateralThoughtsClientAnalyzer(luceneVersion)

  initWithDocuments()


  def initWithDocuments() {
    val writer = openWriter
    clients.foreach { case (id : Int, client : ClientDefinition) => writeDocument(writer, id, client)}
    writer.close(true)
  }

  def addToIndex(id: Int, client : ClientDefinition) {
    val writer = openWriter
    writeDocument(writer, id, client)
    writer.close(true)
  }

  def createSearchQuery(q: String) = {
    val query = new PhraseQuery()
    query.setSlop(10)

    val tokenStream = docAnalyzer.tokenStream("text", q)
    val offsetAttribute = tokenStream.addAttribute(classOf[OffsetAttribute])
    val charTermAttribute = tokenStream.addAttribute(classOf[CharTermAttribute])

    tokenStream.reset();
    while (tokenStream.incrementToken()) {
      val startOffset = offsetAttribute.startOffset()
      val endOffset = offsetAttribute.endOffset()

      val term = charTermAttribute.toString()
      query.add(new Term("text", term))
    }
    tokenStream.close()

    query
  }

  def search(q: String) : List[ClientDefinition] = {
    val searchQuery = createSearchQuery(q)
    val dirReader = DirectoryReader.open(directory)
    val searcher = new IndexSearcher(dirReader)
    val results = searcher.search(searchQuery, MAX_NUMBER_OF_DOCS);

    val resultClients = results.scoreDocs.map(
      resultDoc => clients.get(searcher.doc(resultDoc.doc).get("id").toInt).get
    )

    dirReader.close()
    resultClients.toList
  }


  /**
   * Close directory after search is done
   */
  def close { directory.close() }

  private def writeDocument(writer: IndexWriter, id: Int, client: ClientDefinition) {
    writer.addDocument(createDocFromClient(id, client))
  }

  private def openWriter = {
    val config = new IndexWriterConfig(luceneVersion, docAnalyzer)
    new IndexWriter(directory, config)
  }
}

trait ClientDefinitionIndexation {

  def createFieldText(client: ClientDefinition): IndexableField = {
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

  protected def createDocFromClient(id: Int, client: ClientDefinition) : Document = {
    val document = new Document()
    document.add(createFieldStored("id", id.toString))
    document.add(createFieldStored("name", client.name))
    document.add(createFieldText(client))
    document
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