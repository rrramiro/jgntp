package com.google.code.jgntp.internal.message

object GntpId {
  val PREFIX: String = "x-growl-resource://"

  def of(value: String): GntpId = new GntpId(value)
}

case class GntpId(value: String) {

  override def toString: String = GntpId.PREFIX + value
}

