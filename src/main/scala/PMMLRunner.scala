import com.github.tototoshi.csv.*
import java.io.File
import java.util
import org.jpmml.evaluator.*
import scala.jdk.CollectionConverters.*
import scala.util.{Failure, Success, Try, Using}

object PMMLRunner {
  var inputFileName: String = _
  var pmmlFileName: String = _
  var outputFileName: Option[String] = Option.empty[String]

  @main
  def main(args: String*): Unit = {

    // check command line arguments
    if (!Set(4, 6).contains(args.length)) warnAndExit()
    else
      for (i <- args.indices by 2) {
        args(i) match {
          case "-input" =>
            inputFileName = args(i + 1)
          case "-output" =>
            outputFileName = Option(args(i + 1))
          case "-pmml" =>
            pmmlFileName = args(i + 1)
          case _ => warnAndExit()
        }
      }

    println("PMML Runner: Processing started ...")
    (for {
      evaluator <- readPMML(pmmlFileName)
      csv       <- readCSV(inputFileName)
      input     <- preprocess(evaluator, csv)
      output    <- process(evaluator, input)
      _         <- writeOutputFile(outputFileName, output)
    } yield {}) match {
      case Left(errorMessage) =>
        warnAndExit(Option(errorMessage))
      case Right(_) =>
        println("PMML Runner: Processing completed")
        println(s"PMML Runner: Check results in file ${outputFileName.getOrElse("output.txt")}")
    }
  }

  /**
   * Display error message and quit
   *
   * @param message : The message to display
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
   * Read a PMML File and returns either an evaluator or a error message
   *
   * @param filename : Path to PMML file
   * @return Either an ML model evaluator or an error message
   */
  def readPMML(filename: String): Either[String, ModelEvaluator[_]] = Try {
    val pmml: File = File(filename)
    val builder = LoadingModelEvaluatorBuilder()
    builder
      .load(pmml)
      .build
  } match {
    case Failure(ex) => Left(Option(ex.getMessage).map(message => s"PMML File Error => $message").getOrElse("PMML File Error"))
    case Success(value) => Right(value)
  }

   /**
   * Read CSV with headers
   *
   * @param filename : Path to input CSV file
   * @return Either the CSV rows as a List of Maps or an error message
   */
  def readCSV(filename: String): Either[String, List[Map[String, String]]] = Try {
    val reader = CSVReader.open(File(filename))
    reader.allWithHeaders()
  } match {
    case Failure(ex) => Left(Option(ex.getMessage).map(message => s"CSV File Error => $message").getOrElse("CSV File Error"))
    case Success(value) => Right(value)
  }

  def preprocess(evaluator: ModelEvaluator[_], csv_list: List[Map[String, String]]): Either[String, List[util.Map[String, FieldValue]]] = Try {
    csv_list.map(row => {
      val activeFields: List[InputField] = evaluator.getActiveFields.asScala.toList
      activeFields.map(field => {
        val fieldName = field.getName
        val fieldValue = field.prepare(row(fieldName))
        fieldName -> fieldValue
      }).toMap.asJava
    })
  } match {
    case Failure(ex) => Left(Option(ex.getMessage).map(message => s"CSV File Error => $message").getOrElse("CSV File Error"))
    case Success(value) => Right(value)
  }

  def process(evaluator: ModelEvaluator[_], processed: List[util.Map[String, FieldValue]]): Either[String, List[List[Any]]] = Try {
    processed.map(el => evaluator.evaluate(el).values.asScala.toList)
  } match {
    case Failure(ex) => Left(Option(ex.getMessage).map(message => s"Evaluation Error => $message").getOrElse("Error Evaluating Record"))
    case Success(value) => Right(value)
  }

   /**
   * Write output to a file
   *
   * @param filename    : Path to output file
   * @param output_list : result list
   * @return Either Unit or an error message
   */
  def writeOutputFile(filename: Option[String], output_list: List[List[Any]]): Either[String, Unit] =
    Using( CSVWriter.open(File(filename.getOrElse("output.txt"))) ){ _.writeAll(output_list)} match {
      case Failure(ex) => Left(Option(ex.getMessage).map(message => s"Output File Error => $message").getOrElse("Error Writing Output File"))
      case Success(value) => Right(value)
    }
}