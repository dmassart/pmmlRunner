import com.github.tototoshi.csv.*
import java.io.{File, FileInputStream}
import java.util

import org.dmg.pmml.PMML
import org.jpmml.evaluator.*
import org.jpmml.model.PMMLUtil

import scala.jdk.CollectionConverters.*
import scala.util.{Failure, Success, Try}

object PMMLRunner {
  var evaluator: ModelEvaluator[_] = _
  var csv_list: List[Map[String, String]] = _
  var output: Option[String] = Option.empty[String]

  @main
  def main(args: String*): Unit = {

    // check command line arguments
    if (!Set(4, 6).contains(args.length)) warnAndExit()
    else
      for (i <- args.indices by 2) {
        args(i) match {
          case "-input" =>
            csv_list = readCSV(args(i + 1))
          case "-output" =>
            output = Option(args(i + 1))
          case "-pmml" => Try( readPMML(args(i + 1)) ) match {
            case Success(ev) => evaluator = ev
            case Failure(ex) => warnAndExit( Option( s"PMML File Error => ${ex.getMessage}" ) )
          }
          case _ => warnAndExit()
        }
      }

    println("PMML Runner: Processing started ...")
    /* Actual PMML handling starts here */

    // prepare input data (i.e., turn csv into map)
    val scoring_list: List[util.Map[String, FieldValue]] = csv_list.map(row => Try{
      val activeFields: List[InputField] = evaluator.getActiveFields.asScala.toList
      activeFields.map(field => {
        val fieldName = field.getName
        val fieldValue = field.prepare(row(fieldName))
        fieldName -> fieldValue
      }).toMap
    } match {
      case Failure(ex) =>
        warnAndExit( Option(s"Input CSV File Error => ${ex.getMessage}") )
        Map.empty[String, FieldValue].asJava
      case Success(value) =>
        value.asJava
    })

    // Perform scoring using JPMML
    val regular_score_list = scoring_list.map(el => evaluator.evaluate(el).values.asScala.toList)

    // Save the outputs as a CSV
    writeOutputFile(output.getOrElse("output.txt"), regular_score_list)

    /* Actual PMML handling ends here */
    println("PMML Runner: Processing completed")
    println(s"PMML Runner: Check results in file ${output.getOrElse("output.txt")}")
  }

  /**
   * Display error message and quit
   * @param message: The message to display
   */
  def warnAndExit(message: Option[String] = None): Unit = {
    message match {
      case None =>
        System.err.println("Usage: PMMLRunner -input <path to input CSV file> -pmml <path to PMML file> [-output <path to output file>]")
      case Some(error) =>
        System.err.println(error)
    }
    System.exit(-1)
  }

  /**
   * Read a PMML File and returns an evaluator
   * @param filename: Path to PMML file
   * @return a ML model evaluator
   */
  def readPMML(filename: String): ModelEvaluator[_] = {
    val pmml: File = new File(filename)
    val builder = new LoadingModelEvaluatorBuilder()
    builder
      .load(pmml)
      .build
  }

  /**
   * Read CSV with headers
   * @param filename: Path to input CSV file
   * @return List of Map
   */
  def readCSV(filename: String): List[Map[String, String]] = {
    val reader = CSVReader.open(new File(filename))
    reader.allWithHeaders()
  }

  /**
   * Write output in file
   * @param filename: Path to output file
   * @param output_list: result list
   */
  def writeOutputFile(filename: String, output_list: List[List[Any]]): Unit = {
    val f = new File(filename)
    val writer = CSVWriter.open(f)
    writer.writeAll(output_list)
    writer.close
  }
}