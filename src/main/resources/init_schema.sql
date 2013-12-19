-- Simpleton's db for feeding demo info back to client
-- TODO: review value of SQL vs NOSQL, field sizes, etc
-- TODO: reconsider decision to remove foreign keys (reportedly not scalable)

CREATE TABLE IF NOT EXISTS facebook_users (
  -- GRRR, Facebook uses a string in their user object, but int in other links to the user
  fb_user_id		VARCHAR(255) NOT NULL,
  fb_user_name		VARCHAR(255),
  first_name		VARCHAR(128),
  middle_name		VARCHAR(128),
  last_name			VARCHAR(128),
  gender			VARCHAR(16),
  locale			VARCHAR(16),
  birthday			VARCHAR(16),
  email				VARCHAR(384),
  access_token		VARCHAR(255),
  token_expiration 	INT DEFAULT 2147483647,

  PRIMARY KEY  (fb_user_id)
);

CREATE TABLE IF NOT EXISTS facebook_pages (
  id                VARCHAR(255) NOT NULL,
  name              VARCHAR(255),
  about             TEXT,
  general_info      TEXT,
  description       TEXT,
  company_overview  TEXT,
  mission           TEXT,
  awards            TEXT,
  fan_count         INT,
  talking_about_count INT,
  can_post          BOOLEAN,
  page_url          TEXT,
  cover_photo_src   TEXT,
  pic_large_src     TEXT,
  pic_square_src    TEXT,

  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS user_page_likes (
  fb_user_id        VARCHAR(255) NOT NULL,
  fb_page_id        VARCHAR(255) NOT NULL,

  UNIQUE KEY idx_user_page_like (fb_user_id, fb_page_id)
);

-- NOTE: only one pic per actor
CREATE TABLE IF NOT EXISTS  facebook_feed_actor_profiles (
  actor_id	    	VARCHAR(255) NOT NULL,
  name              VARCHAR(255),
  square_pic_url 	TEXT,

  PRIMARY KEY  (actor_id)
);

CREATE TABLE IF NOT EXISTS  facebook_user_friends (
  fb_user_id		VARCHAR(255) NOT NULL,
  friend_fb_id		VARCHAR(255) NOT NULL,

  UNIQUE KEY idx_unique_user_friend (fb_user_id, friend_fb_id)
);

CREATE TABLE IF NOT EXISTS  cc_activations (
  activation_id 	INT UNSIGNED NOT NULL AUTO_INCREMENT,
  app_name 			VARCHAR(64),
  app_version 		VARCHAR(32),
  device_type 		VARCHAR(128),
  device_id 		VARCHAR(255),
  device_name 		VARCHAR(255),
  device_os_version VARCHAR(32),
  created_time 		TIMESTAMP NOT NULL,

  PRIMARY KEY  (activation_id),
  UNIQUE KEY idx_unique_activation (app_name, app_version, device_id, device_os_version)
);

CREATE TABLE IF NOT EXISTS  look_feel (
  look_feel_id 		INT UNSIGNED NOT NULL AUTO_INCREMENT,
  name		 		VARCHAR(255),

  PRIMARY KEY(look_feel_id)
);

-- NOTE: these will come from existing branding info, but for now this is just a dictionary
-- TODO: need to enforce unique id+name combo

CREATE TABLE IF NOT EXISTS  look_feel_values (
  id 				INT UNSIGNED NOT NULL AUTO_INCREMENT,
  look_feel_id 		INT,
  attribute_name 	VARCHAR(255),
  attribute_value	VARCHAR(255),

  PRIMARY KEY(id)
);

CREATE TABLE IF NOT EXISTS  comments (
  id				VARCHAR(255) NOT NULL,
  object_id			VARCHAR(255) NOT NULL,
  from_id			VARCHAR(255),
  created_time		INT,
  text				TEXT,
  like_count		INT,
  user_likes        BOOLEAN,
  can_like          BOOLEAN,
  is_private        BOOLEAN,

  PRIMARY KEY(id),
  KEY idx_object_comments (object_id)
);

-- NOTE: facebook's pid is a photo id, but can't be used
--   for posting comments, so we use 'fbid' as our object_id
CREATE TABLE IF NOT EXISTS  photos (
  object_id			VARCHAR(255) NOT NULL,
  pid               VARCHAR(255) NOT NULL,
  album_object_id	VARCHAR(255) NOT NULL,
  aid               VARCHAR(255),
  caption           TEXT,
  album_index		INT,
  owner_id          VARCHAR(255) NOT NULL,
  can_delete        BOOLEAN,
  modified_time     INT,
  target_id         VARCHAR(255),
  place_id          VARCHAR(255),

  comment_count     INT,
  can_comment       BOOLEAN,

  like_count        INT,
  user_likes        BOOLEAN,
  can_like          BOOLEAN,

  PRIMARY KEY(object_id),
  KEY idx_photo_album_id (album_object_id)
);

CREATE TABLE IF NOT EXISTS albums (
  aid			    VARCHAR(255) NOT NULL,
  object_id			VARCHAR(255) NOT NULL,
  name              TEXT,
  description       TEXT,
  cover_pid         VARCHAR(255),
  cover_object_id   VARCHAR(255),
  owner_id          VARCHAR(255) NOT NULL,
  created_time		INT,
  modified_time		INT,
  modified_major_time    INT,
  location          VARCHAR(255),
  place_id          VARCHAR(255),
  can_upload        BOOLEAN,
  is_user_facing    BOOLEAN,

  comment_count     INT,
  can_comment       BOOLEAN,

  like_count        INT,
  user_likes        BOOLEAN,
  can_like          BOOLEAN,

  PRIMARY KEY(object_id),
  KEY idx_album_owner_id (owner_id)
);

CREATE TABLE IF NOT EXISTS  photo_images (
  id                INT UNSIGNED NOT NULL AUTO_INCREMENT,
  photo_id          VARCHAR(255) NOT NULL,
  height            INT,
  width             INT,
  source            TEXT,

  PRIMARY KEY(id),
  KEY idx_image_photo_id (photo_id),
  UNIQUE KEY idx_unique_image_size (photo_id, height, width)
);

CREATE TABLE IF NOT EXISTS  stream_feeds (
  id				VARCHAR(255) NOT NULL,
  updated_time		INT,
  created_time		INT,
  actor_id			VARCHAR(255),
  message			TEXT,
  description		TEXT,
  feed_type			INT,
  target_id         VARCHAR(255),

  attachment_name				TEXT,
  attachment_caption			TEXT,
  attachment_description		TEXT,
  attachment_href		        TEXT,
  attachment_fb_object_type	    VARCHAR(255),
  attachment_fb_object_id		VARCHAR(255),

  comment_count     INT,
  can_comment       BOOLEAN,

  like_count        INT,
  user_likes        BOOLEAN,
  can_like          BOOLEAN,

  PRIMARY KEY(id)
);

CREATE TABLE IF NOT EXISTS  parent_stream_feeds (
  parent_id        VARCHAR(255) NOT NULL,
  stream_feed_id    VARCHAR(255) NOT NULL,

  UNIQUE KEY idx_unique_parent_feed (parent_id, stream_feed_id)
);

-- NOTE: likes have no privacy, so viewer_id is not a filter
CREATE TABLE IF NOT EXISTS  object_user_likes (
  object_id         VARCHAR(255) NOT NULL,
  fb_user_id        VARCHAR(255) NOT NULL,

  KEY(object_id),
  UNIQUE KEY idx_unique_user_likes (object_id, fb_user_id)
);

CREATE TABLE IF NOT EXISTS  stream_feed_media (
  stream_feed_id    VARCHAR(255) NOT NULL,
  media_type		VARCHAR(255),
  media_src			TEXT,
  media_href	    TEXT,
  media_id          VARCHAR(255),

  KEY(stream_feed_id)
);