package org.json4s

class ExtractableJsonAstNode(jv: JValue) {
  /**
   * Extract a value from a JSON.
   * <p>
   * Value can be:
   * <ul>
   *   <li>case class</li>
   *   <li>primitive (String, Boolean, Date, etc.)</li>
   *   <li>supported collection type (List, Seq, Map[String, _], Set)</li>
   *   <li>any type which has a configured custom deserializer</li>
   * </ul>
   * <p>
   * Example:<pre>
   * case class Person(name: String)
   * JObject(JField("name", JString("joe")) :: Nil).extract[Person] == Person("joe")
   * </pre>
   */
  def extract[A](implicit formats: Formats, mf: scala.reflect.Manifest[A]): A =
    Extraction.extract(jv)(formats, mf)

  /**
   * Extract a value from a JSON.
   * <p>
   * Value can be:
   * <ul>
   *   <li>case class</li>
   *   <li>primitive (String, Boolean, Date, etc.)</li>
   *   <li>supported collection type (List, Seq, Map[String, _], Set)</li>
   *   <li>any type which has a configured custom deserializer</li>
   * </ul>
   * <p>
   * Example:<pre>
   * case class Person(name: String)
   * JObject(JField("name", JString("joe")) :: Nil).extractOpt[Person] == Some(Person("joe"))
   * </pre>
   */
  def extractOpt[A](implicit formats: Formats, mf: scala.reflect.Manifest[A]): Option[A] =
    Extraction.extractOpt(jv)(formats, mf)

  /**
   * Extract a value from a JSON using a default value.
   * <p>
   * Value can be:
   * <ul>
   *   <li>case class</li>
   *   <li>primitive (String, Boolean, Date, etc.)</li>
   *   <li>supported collection type (List, Seq, Map[String, _], Set)</li>
   *   <li>any type which has a configured custom deserializer</li>
   * </ul>
   * <p>
   * Example:<pre>
   * case class Person(name: String)
   * JNothing.extractOrElse(Person("joe")) == Person("joe")
   * </pre>
   */
  def extractOrElse[A](default: ⇒ A)(implicit formats: Formats, mf: scala.reflect.Manifest[A]): A =
    Extraction.extractOpt(jv)(formats, mf).getOrElse(default)

  /**
   * Given that an implicit reader of type `A` is in scope
   * It will deserialize the [[org.json4s.JsonAST.JValue]] to an object of type `A`
   *
   * Example:
   * {{{
   *   case class Person(name: String)
   *   implicit object PersonReader extends Reader[Person] {
   *     def read(json: JValue): Person = Person((json \ "name").extract[String])
   *   }
   *   JObject(JField("name", JString("Joe")) :: Nil).as[Person]
   * }}}
   */
  def as[A](implicit reader: Reader[A], mf: Manifest[A]): A = reader.read(jv)

  /**
   * Given that an implicit reader of type `A` is in scope
   * It will deserialize the [[org.json4s.JsonAST.JValue]] to an object of type Option[`A`]
   *
   * Example:
   * {{{
   *   case class Person(name: String)
   *   implicit object PersonReader extends Reader[Person] {
   *     def read(json: JValue): Person = Person((json \ "name").extract[String])
   *   }
   *   JObject(JField("name", JString("Joe")) :: Nil).getAs[Person]
   * }}}
   */
  def getAs[A](implicit reader: Reader[A], mf: scala.reflect.Manifest[A]): Option[A] = try {
    Option(reader.read(jv))
  } catch { case _: Throwable ⇒ None }

  /**
   * Given that an implicit reader of type `A` is in scope
   * It will deserialize the [[org.json4s.JsonAST.JValue]] to an object of type `A` 
   * if an error occurs it will return the default value.
   *
   * Example:
   * {{{
   *   case class Person(name: String)
   *   implicit object PersonReader extends Reader[Person] {
   *     def read(json: JValue): Person = Person((json \ "name").extract[String])
   *   }
   *   JObject(JField("name", JString("Joe")) :: Nil).getAsOrElse(Person("Tom"))
   * }}}
   */
  def getAsOrElse[A](default: ⇒ A)(implicit reader: Reader[A], mf: Manifest[A]): A =
    getAs(reader, mf) getOrElse default

  /**
   * Extract a value from a JSON with the given function f.
   *
   * Example:<pre>
   * parse("""{"name":"john","age":32}""") extract { jv =>
   *   val name = (jv \ "name").extract[String]
   *   val age = (jv \ "age").extract[Int]
   *   (name, age)
   * } == ("john", 32)</pre>
   */
  def extract[A](f: JValue => A): A = f(jv)

  /**
   * Extract a value from a JSON with the given function f.
   *
   * Example:<pre>
   * parse("""{"name":"john","age":32}""") extractOpt { jv =>
   *   val name = (jv \ "name").extract[String]
   *   val age = (jv \ "age").extract[Int]
   *   Some((name, age))
   * } == Some(("john", 32))</pre>
   */
  def extractOpt[A](f: JValue => Option[A]): Option[A] = f(jv)

  /**
   * Extract a list of values from a JSON with the given function f, if the value is a JArray.
   * Returns the empty list otherwise.
   *
   * Example:<pre>
   * parse("""[{"name":"john","age":32}, {"name":"joe","age":23}]""") extractList { jv =>
   *   val name = (jv \ "name").extract[String]
   *   val age = (jv \ "age").extract[Int]
   *   (name, age)
   * } == List(("john", 32), ("joe", 23))</pre>
   */
  def extractList[A](f: JValue => A): List[A] = jv match {
    case JArray(values) => values map f
    case _ => List.empty
  }

}

