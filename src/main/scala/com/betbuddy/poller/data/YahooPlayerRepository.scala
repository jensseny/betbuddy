package com.betbuddy.poller.data

import javax.sql.DataSource
import com.betbuddy.util.JDBCHelpers
import com.betbuddy.poller.services.player.YahooBatter

class YahooPlayerRepository (dataSource: DataSource) {

  def saveBatter(batter: YahooBatter) {
    JDBCHelpers.withConnection(dataSource) { conn =>

    }
//    def save (cruisebookPhoto: CruisebookPhoto, conn: Connection) {
//      // NOTE: rather than replacing any existing content for this feed, we delete and insert
//      deleteByFacebookObjectId(cruisebookPhoto.facebook_photo.object_id, conn)
//      val updateString = "INSERT INTO cruisebook_photos (" +
//        "cruisebook_id, cruisebook_update_time, cruisebook_viewer_id" +
//        ", facebook_object_id, facebook_pid, facebook_album_object_id, facebook_aid, facebook_caption, facebook_album_index" +
//        ", facebook_owner_id, facebook_modified_time, facebook_target_id, facebook_place_id" +
//        ", can_delete, comment_count, can_comment" +
//        ", like_count, user_likes, can_like" +
//        ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
//
//      JDBCHelpers.update(conn, updateString, Seq(cruisebookPhoto.cruisebook_id, cruisebookPhoto.cruisebook_update_time, cruisebookPhoto.cruisebook_viewer_id,
//        cruisebookPhoto.facebook_photo.object_id, cruisebookPhoto.facebook_photo.pid,
//        cruisebookPhoto.facebook_photo.album_object_id, cruisebookPhoto.facebook_photo.aid,
//        cruisebookPhoto.facebook_photo.caption.map(UTF8Filter.stripNonBMP).getOrElse(null), cruisebookPhoto.facebook_photo.position,
//        cruisebookPhoto.facebook_photo.owner, cruisebookPhoto.facebook_photo.modified,
//        cruisebookPhoto.facebook_photo.target_id, cruisebookPhoto.facebook_photo.place_id,
//        cruisebookPhoto.facebook_photo.can_delete, cruisebookPhoto.facebook_photo.comment_info.flatMap(_.comment_count).getOrElse(0),
//        cruisebookPhoto.facebook_photo.comment_info.flatMap(_.can_comment).getOrElse(false),
//        cruisebookPhoto.facebook_photo.like_info.flatMap(_.like_count).getOrElse(0),
//        cruisebookPhoto.facebook_photo.like_info.flatMap(_.user_likes).getOrElse(false),
//        cruisebookPhoto.facebook_photo.like_info.flatMap(_.can_like).getOrElse(false)))
  }
}
