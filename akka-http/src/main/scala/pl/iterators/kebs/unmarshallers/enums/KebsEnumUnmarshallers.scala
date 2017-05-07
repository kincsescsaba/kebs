package pl.iterators.kebs.unmarshallers.enums

import akka.http.scaladsl.unmarshalling.{FromStringUnmarshaller, Unmarshaller}
import akka.http.scaladsl.util.FastFuture
import enumeratum.values.{ValueEnum, ValueEnumEntry}
import enumeratum.{Enum, EnumEntry}
import pl.iterators.kebs.unmarshallers.KebsFromStringUnmarshallers
import pl.iterators.kebs.unmarshallers.KebsUnmarshallers.InvariantDummy

trait EnumUnmarshallers {
  final def enumUnmarshaller[E <: EnumEntry](enum: Enum[E]): FromStringUnmarshaller[E] = Unmarshaller { _ => name =>
    enum.withNameInsensitiveOption(name) match {
      case Some(enumEntry) => FastFuture.successful(enumEntry)
      case None =>
        FastFuture.failed(new IllegalArgumentException(s"""Invalid value '$name'. Expected one of: ${enum.namesToValuesMap.keysIterator
          .mkString(", ")}"""))
    }
  }
}

trait ValueEnumUnmarshallers extends KebsFromStringUnmarshallers {
  final def valueEnumUnmarshaller[V, E <: ValueEnumEntry[V]](enum: ValueEnum[V, E]): Unmarshaller[V, E] = Unmarshaller { _ => v =>
    enum.withValueOpt(v) match {
      case Some(enumEntry) => FastFuture.successful(enumEntry)
      case None =>
        FastFuture.failed(new IllegalArgumentException(s"""Invalid value '$v'. Expected one of: ${enum.valuesToEntriesMap.keysIterator
          .mkString(", ")}"""))
    }
  }
}

trait KebsEnumUnmarshallers extends EnumUnmarshallers with ValueEnumUnmarshallers {
  import macros.KebsEnumUnmarshallersMacros
  implicit def kebsEnumUnmarshaller[E <: EnumEntry]: FromStringUnmarshaller[E] =
    macro KebsEnumUnmarshallersMacros.materializeEnumUnmarshaller[E]
  implicit def kebsValueEnumUnmarshaller[V: InvariantDummy, E <: ValueEnumEntry[V]]: Unmarshaller[V, E] =
    macro KebsEnumUnmarshallersMacros.materializeValueEnumUnmarshaller[V, E]

}
