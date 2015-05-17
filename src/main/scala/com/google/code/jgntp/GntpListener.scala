package com.google.code.jgntp

import com.google.code.jgntp.internal.GntpErrorStatus.GntpErrorStatus


trait GntpListener {
  def onRegistrationSuccess

  def onNotificationSuccess(notification: GntpNotification)

  def onClickCallback(notification: GntpNotification)

  def onCloseCallback(notification: GntpNotification)

  def onTimeoutCallback(notification: GntpNotification)

  def onRegistrationError(status: GntpErrorStatus, description: String)

  def onNotificationError(notification: GntpNotification, status: GntpErrorStatus, description: String)

  def onCommunicationError(t: Throwable)
}