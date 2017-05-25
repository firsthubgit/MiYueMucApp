package com.greendao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Config;

import com.miyue.application.DbConstans;

import java.security.PublicKey;
import java.util.List;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.DaoException;
import de.greenrobot.dao.query.QueryBuilder;

/**
 * Created by zhangzhendong on 16/4/21.
 */
public class DBHelper {
    private DBHelper(){}

    private static volatile DBHelper instance = null;
    private static final String DB_NAME = DbConstans.DBNAME;
    private SQLiteDatabase db;
    private DaoSession daoSession;

    private LocalMusicDao localMusicDao;
    private DownloadDao downloadDao;
    private FavoritesDao favoritesDao;
    private RecentDao recentDao;

    public static DBHelper getInstance(Context mContext){
        if(null == instance){
            synchronized (DBHelper.class){
                if(null == instance){
                    instance = new DBHelper(mContext);
                }
            }
        }
        return instance;
    }

    private DBHelper(Context context) {
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(context, DB_NAME, null);
        db = helper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(db);
        daoSession = daoMaster.newSession();
        initDao();
    }

    private void initDao() {
        localMusicDao = daoSession.getLocalMusicDao();
        downloadDao = daoSession.getDownloadDao();
        favoritesDao = daoSession.getFavoritesDao();
        recentDao = daoSession.getRecentDao();
    }

    /**
     * 向表中添加数据
     * @param entity 音乐实体对象
     * @param tableName 表的名字
     * */
    public void addMusic(MusicBean entity,String tableName){
        switch (tableName){
            case DbConstans.LOCAL_MUSIC:
                localMusicDao.insert(entity);
                break;
            case DbConstans.DOWNLOAD:
                downloadDao.insert(entity);
                break;
            case DbConstans.FAVORITES:
                favoritesDao.insert(entity);
                break;
            case DbConstans.RECENT:
                recentDao.insert(entity);
                break;
        }
    }


    /**
     * 删除下载表中的数据
     * @param entity 音乐实体对象
     * */
    public void deleteDownload(MusicBean entity) throws DaoException{
        daoSession.getDownloadDao().delete(entity);
    }

    /**
     * 删除喜欢表中的数据
     * @param entity 音乐实体对象
     * */
    public void deleteFavorites(MusicBean entity) throws DaoException{
        daoSession.getFavoritesDao().delete(entity);
    }
    /*****************************************************************************/
    //以下是对本地音乐的操作
    /**
     * 获取本地音乐表中的所有数据
     * @return List<LocalMusic>  返回数据库中查询到是所有数据
     * */
    public List<MusicBean> getLocalMusicList(){
        return daoSession.getLocalMusicDao().loadAll();
    }

    public long getLocalMusicCount(){
        return daoSession.getLocalMusicDao().count();
    }

    /*
    * 删除本地音乐表中的某条数据
    * @param entity 音乐实体对象
    */
    public void deleteLocalMusic(MusicBean entity) throws DaoException{
        daoSession.getLocalMusicDao().delete(entity);
    }

    /*****************************************************************************/
    //以下是对最近播放音乐的操作

    /**
     * 获取本地音乐表中的所有数据
     * @return List<LocalMusic>  返回数据库中查询到是所有数据
     * */
    public List<MusicBean> getRecentMusicList(){
        return daoSession.getRecentDao().loadAll();
    }

    public long getRecnetMusicCount(){
        return daoSession.getRecentDao().count();
    }

    /**
     * 删除最近表中的数据
     * @param entity 音乐实体对象
     * */
    public void deleteRecent(MusicBean entity) throws DaoException{
        daoSession.getRecentDao().delete(entity);
    }
}
