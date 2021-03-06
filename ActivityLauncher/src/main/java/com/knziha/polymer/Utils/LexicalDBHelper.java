package com.knziha.polymer.Utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.os.CancellationSignal;
import android.util.SparseArray;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.knziha.polymer.widgets.Utils.EmptyCursor;

/**
 * Created by KnIfER on 2020/7/8.
 */

public class LexicalDBHelper extends SQLiteOpenHelper {
    public final String DATABASE;
	private SQLiteDatabase database;
	private boolean history_active_updated;
	private boolean history_empty_updated;
	private boolean bookmark_active_updated;
	private boolean bookmark_empty_updated;
	private boolean search_active_updated;
	private boolean search_empty_updated;
	private List<Cursor> cursors_to_close = Collections.synchronizedList(new ArrayList<>());
	
	public SQLiteDatabase getDB(){return database;}
    
    public static final String HISTORY_SEARCH = "t1";

    public static String Key_ID = "lex"; //主键
    public static final String Date = "date"; //路径
    
    public String pathName;

	/** 创建历史纪录数据库 */
	public LexicalDBHelper(Context context, Options opt) {
		super(context, new File(context.getExternalFilesDir(null), "history.sql").getPath(), null, CMN.dbVersionCode);
		DATABASE="history.sql";
		onConfigure();
	}
	
	void onConfigure() {
		database = getWritableDatabase();
		pathName = database.getPath();
		oldVersion=CMN.dbVersionCode;
	}
	
	@Override
    public void onCreate(SQLiteDatabase db) {
		CMN.Log("onDbCreate");
		final String createhistory = "create table if not exists urls(" +
				"id INTEGER PRIMARY KEY AUTOINCREMENT," +
				"url LONGVARCHAR," +
				"title LONGVARCHAR," +
				"visit_count INTEGER DEFAULT 0 NOT NULL," +
				"note_id INTEGER DEFAULT -1 NOT NULL,"+
				"last_visit_time INTEGER NOT NULL)";
        db.execSQL(createhistory);
		
		final String createsearch = "create table if not exists \"keyword_search_terms\" ("+
				"id INTEGER PRIMARY KEY AUTOINCREMENT," +
				"term LONGVARCHAR NOT NULL,"+
				"last_visit_time INTEGER NOT NULL)";
		
		db.execSQL(createsearch);
		
		final String createnote = "create table if not exists \"annots\" ("+
				"id INTEGER PRIMARY KEY AUTOINCREMENT," +
				"url LONGVARCHAR," +
				"notes LONGVARCHAR,"+
				"texts LONGVARCHAR,"+
				"url_id INTEGER DEFAULT -1 NOT NULL,"+
				"creation_time INTEGER DEFAULT 0 NOT NULL," +
				"last_visit_time INTEGER NOT NULL)";
		
		db.execSQL(createnote);
		
		db.execSQL("CREATE INDEX if not exists urls_url_index ON urls (url)");
		db.execSQL("CREATE INDEX if not exists annots_url_index ON annots (url)");
		db.execSQL("CREATE INDEX if not exists keyword_search_terms_index3 ON keyword_search_terms (term)");
		
		CMN.Log("onDbCreate..done");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int _oldVersion, int newVersion) {
        //在 setVersion 前已经调用
		onCreate(db);
        //oldVersion=_oldVersion;
		db.setVersion(oldVersion=newVersion);
        CMN.Log("onUpgrade",oldVersion+":"+newVersion+":"+db.getVersion());
    }

    //lazy Upgrade
	boolean isDirty=false;
    int oldVersion=1;
    @Override
    public void onOpen(SQLiteDatabase db) {
        db.setVersion(oldVersion);
    }
	
	Cursor ActiveUrlCursor;
	Cursor ActiveSearchCursor;
	public Cursor EmptySearchCursor;
	private String LastSearchTerm;
	StringBuilder MainBuilder = new StringBuilder(128);
 
	public Cursor queryUrl(String url_key, int limitation, CancellationSignal stopSign) {
		if(StringUtils.isEmpty(url_key)) {
			return EmptyCursor;
		}
    	if(ActiveUrlCursor!=null) {
			if(url_key.equals(LastSearchTerm)&&!history_active_updated) {
				return ActiveUrlCursor;
			}
			cursors_to_close.add(ActiveUrlCursor);
			ActiveUrlCursor = null;
		}
		String sql = "select url,title from urls where url like ? or title like ?  order by visit_count desc, last_visit_time desc";
		MainBuilder.setLength(0);
		String term = MainBuilder.append("%").append(url_key).append("%").toString();
		if(limitation!=Integer.MAX_VALUE) {
			if(limitation<=0) {
				return EmptyCursor;
			}
			MainBuilder.setLength(0);
			sql = MainBuilder.append(sql).append(" limit ").append(limitation).toString();
		}
		Cursor activeUrlCursor = database.rawQuery(sql, new String[]{term, term}, stopSign);
		//Cursor activeUrlCursor = database.query(true, "urls", new String[]{"url","title"}, "url like ? or title like ?",new String[]{term, term}, null, null,"visit_count desc, last_visit_time desc", "0,8",stopSign);
		history_active_updated =false;
		return ActiveUrlCursor = activeUrlCursor;
	}
	
	public Cursor querySearchTerms(String url_key, int limitation, CancellationSignal stopSign) {
		if(StringUtils.isEmpty(url_key)) {
			if(EmptySearchCursor!=null) {
				if(!search_empty_updated) {
					return EmptySearchCursor;
				}
				cursors_to_close.add(EmptySearchCursor);
				EmptySearchCursor=null;
			}
			String sql = "select term from keyword_search_terms";
//			if(limitation!=Integer.MAX_VALUE) {
//				if(limitation<=0) {
//					return EmptyCursor;
//				}
//				MainBuilder.setLength(0);
//				sql = MainBuilder.append(sql).append(" limit ").append(limitation).toString();
//			}
			Cursor emptySearchCursor = database.rawQuery(sql, null ,stopSign);
			search_empty_updated=false;
			return EmptySearchCursor=emptySearchCursor;
		} else {
			if(ActiveSearchCursor!=null) {
				if(url_key.equals(LastSearchTerm)&&!search_active_updated) {
					return ActiveSearchCursor;
				}
				cursors_to_close.add(ActiveSearchCursor);
				ActiveSearchCursor=null;
			}
			String sql = "select term from keyword_search_terms where term like ? order by last_visit_time desc";
			MainBuilder.setLength(0);
			String term = MainBuilder.append(url_key).append("%").toString();
			if(limitation!=Integer.MAX_VALUE) {
				if(limitation<=0) {
					return EmptyCursor;
				}
				MainBuilder.setLength(0);
				sql = MainBuilder.append(sql).append(" limit ").append(limitation).toString();
			}
			Cursor activeUrlCursor = database.rawQuery(sql, new String[]{term}, stopSign);
			search_active_updated=false;
			return ActiveSearchCursor = activeUrlCursor;
		}
	}
	
	public Cursor queryNote(String url_key) {
		String sql = "select notes,texts,url,url_id from annots where url=?";
		return database.rawQuery(sql, new String[]{url_key});
	}
	
	public void updateLastSearchTerm(String url_key) {
		LastSearchTerm = url_key;
	}
	
	public long insertUpdate (String lex, String tile) {
		history_empty_updated=history_active_updated=true;
    	int count=-1;
		int id=-1;
		final String sql = "select id,visit_count from urls where url = ? ";
		String[] where = new String[]{lex};
		Cursor c = database.rawQuery(sql, where);
		if(c.getCount()>0) {
			c.moveToFirst();
			id = c.getInt(0);
			count = c.getInt(1);
		}
		c.close();
		
		boolean notUpdateHistory=false;
		
		if(count>0&&notUpdateHistory) {
			return 1;
		}
		
		//CMN.Log("countcount", count);
		
		ContentValues values = new ContentValues();
		values.put("url", lex);
		values.put("title", tile);
		values.put("visit_count", ++count);
		values.put("last_visit_time", System.currentTimeMillis());
		
		if(count>0) {
			values.put("id", id);
			//database.update("urls", values, "url=?", where);
			database.insertWithOnConflict("urls", null, values, SQLiteDatabase.CONFLICT_REPLACE);
		} else {
			database.insert("urls", null, values);
		}
		return count;
	}
	
	public long insertSearchTerm(String lex) {
		search_empty_updated=search_active_updated=true;
		ContentValues values = new ContentValues();
		values.put("term", lex);
		values.put("last_visit_time", System.currentTimeMillis());
		String[] where = new String[]{lex};
		int count=0;
//		final String sql = "select term from keyword_search_terms where term=?";
//		Cursor c = database.rawQuery(sql, where);
//		count = c.getCount();
//		c.close();
		database.delete("keyword_search_terms", "term=?", where);
		if(count==0) {
			return database.insert("keyword_search_terms", null, values);
		}
		return count;
	}
	
	public long insertUpdateNote (String lex, String notes, String texts) {
		history_empty_updated=history_active_updated=true;
		long id=-1;
		final String sql = "select id from urls where url = ? ";
		String[] where = new String[]{lex};
		Cursor c = database.rawQuery(sql, where);
		if(c.getCount()>0) {
			c.moveToFirst();
			id = c.getInt(0);
		}
		c.close();
		
		ContentValues values = new ContentValues();
		values.put("url", lex);
		values.put("notes", notes);
		values.put("texts", texts);
		values.put("last_visit_time", System.currentTimeMillis());
		
		if(id>=0) {
			values.put("id", (int)id);
			//id = database.update("annots", values, "id=?", new String[]{Integer.toString((int) id)});
			id = database.insertWithOnConflict("annots", null, values, SQLiteDatabase.CONFLICT_REPLACE);
		} else {
			values.put("creation_time", System.currentTimeMillis());
			id = database.insert("annots", null, values);
		}
		return id;
	}
	
	SQLiteStatement preparedSelectExecutor;
	private void prepareContain() {
		if(preparedSelectExecutor==null) {
	     	String sql = "select * from urls where url = ? ";
			preparedSelectExecutor = database.compileStatement(sql);
		}
	}
	
	@Override
	public void close(){
		super.close();
		CMN.Log("onDbClose");
		if(preparedSelectExecutor!=null)
			preparedSelectExecutor.close();
		if(ActiveSearchCursor!=null)
			ActiveSearchCursor.close();
		if(EmptySearchCursor!=null)
			EmptySearchCursor.close();
		if(ActiveUrlCursor!=null)
			ActiveUrlCursor.close();
	}

	public boolean wipeData() {
		return database.delete(HISTORY_SEARCH, null, null)>0;
	}
	
	SparseArray<Object[]> searchcursoremptycahce = new SparseArray<>();
	SparseArray<Object[]> searchcursoractivecahce = new SparseArray<>();
	
	public Object[] getActiveSearchTermAt(Cursor cursor, int position) {
		Object[] ret=null;
		SparseArray<Object[]> cursorcahce=null;
		if(cursor==ActiveSearchCursor) {
			cursorcahce=searchcursoremptycahce;
		} else if(cursor==EmptySearchCursor) {
			searchcursoractivecahce=searchcursoractivecahce;
		}
		ret=cursorcahce.get(position);
		if(ret==null) {
			cursor.moveToPosition(position);
			ret=new Object[]{cursor.getString(0)};
		}
		return ret;
	}
	
	
	public void closeCursors() {
		for(Cursor cursor:cursors_to_close) {
			cursor.close();
		}
		cursors_to_close.clear();
	}
	
	public boolean isOpen() {
		return database.isOpen();
	}
}
