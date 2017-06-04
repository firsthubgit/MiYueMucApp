package com.greendao;

import android.content.Context;
import android.content.Entity;
import android.database.sqlite.SQLiteDatabase;
import android.util.Config;

import com.miyue.application.DbConstans;
import com.miyue.utils.UtilLog;

import java.security.PublicKey;
import java.util.List;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.DaoException;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.query.DeleteQuery;
import de.greenrobot.dao.query.Query;
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
    private SearchHisDao searchHisDao;

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
        searchHisDao = daoSession.getSearchHisDao();
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
                recentDao.insertOrReplace(entity);
                break;
        }
    }


    /**
     *
     * @param
     * @param tableName 表的名字
     * */
    public MusicBean getMusic(String mediaId, String tableName){
        List<MusicBean> list = null;
        switch (tableName){
            case DbConstans.LOCAL_MUSIC:
                list = localMusicDao.queryBuilder().where(LocalMusicDao.Properties.MediaID.eq(mediaId)).list();
                break;
            case DbConstans.DOWNLOAD:
                list = downloadDao.queryBuilder().where(DownloadDao.Properties.MediaID.eq(mediaId)).list();
                break;
            case DbConstans.FAVORITES:
                list = favoritesDao.queryBuilder().where(FavoritesDao.Properties.MediaID.eq(mediaId)).list();
                break;
            case DbConstans.RECENT:
                list = recentDao.queryBuilder().where(RecentDao.Properties.MediaID.eq(mediaId)).list();
                break;
        }
        if(list != null && list.size()>0){
            return list.get(0);
        }
        return null;
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

    /**
     * 删除本地播放表中的数据
     * @param mediaID
     * */
    public void deleteLocal(String mediaID) throws DaoException{
        MusicBean bean = getMusic(mediaID, DbConstans.LOCAL_MUSIC);
        if(null != bean) {
            daoSession.getLocalMusicDao().delete(bean);
        }
    }

    /*****************************************************************************/
    //以下是对下载音乐的操作
    /**
     * 获取下载音乐表中的所有数据
     * @return List<MusicBean>  返回数据库中查询到是所有数据
     * */
    public List<MusicBean> getDownLoadMusicList(){
        return daoSession.getDownloadDao().loadAll();
    }

    public long getDownLoadMusicCount(){
        return daoSession.getDownloadDao().count();
    }

    /**
     * 删除下载表中的数据
     * @param entity 音乐实体对象
     * */
    public void deleteDownload(MusicBean entity) throws DaoException{
        daoSession.getDownloadDao().delete(entity);
    }

    /**
     * 删除下载表中的数据
     * @param mediaID
     * */
    public void deleteDownload(String mediaID) throws DaoException{
        MusicBean bean = getMusic(mediaID, DbConstans.DOWNLOAD);
        if(null != bean){
            daoSession.getDownloadDao().delete(bean);
        }
    }

    /*****************************************************************************/
    //以下是对最近播放音乐的操作

    /**
     * 获取最近播放表中的所有数据
     * @return List<LocalMusic>  返回数据库中查询到是所有数据
     * */
    public List<MusicBean> getRecentMusicList(){
        return daoSession.getRecentDao().loadAll();
    }

    public long getRecnetMusicCount(){
        return daoSession.getRecentDao().count();
    }

    /**
     * 删除最近播放表中的数据
     * @param entity 音乐实体对象
     * */
    public void deleteRecent(MusicBean entity) throws DaoException{
        daoSession.getRecentDao().delete(entity);
    }

    /**
     * 删除最近播放表中的数据
     * @param mediaID
     * */
    public void deleteRecent(String mediaID) throws DaoException{
        MusicBean bean = getMusic(mediaID, DbConstans.RECENT);
        if(null != bean){
            daoSession.getRecentDao().delete(bean);
        }
    }

    /**
     *  删除最近播放表中的第一条数据
     * */
    public void deleteFirstRecent() throws DaoException{
        if(daoSession.getRecentDao().count()>0){
            Query<MusicBean> bd  =daoSession.getRecentDao()
                    .queryBuilder().limit(1).build();
            bd.setLimit(1);
            MusicBean musicBean = bd.unique();
            if(musicBean != null){
                deleteRecent(musicBean);
            }
        }
    }


    /*****************************************************************************/
    //以下是对我喜欢的音乐的操作


    public long getFavoriteMusicCount(){
        return daoSession.getFavoritesDao().count();
    }

    /**
     * 获取我喜欢的表中的所有数据
     * @return List<MusicBean>  返回数据库中查询到是所有数据
     * */
    public List<MusicBean> getFavoriteList(){
        return daoSession.getFavoritesDao().loadAll();
    }

    /**
     * 删除我喜欢的表中的数据
     * @param mediaID
     * */
    public void deleteFavorite(String mediaID) throws DaoException{
        MusicBean bean = getMusic(mediaID, DbConstans.FAVORITES);
        if(null != bean){
            daoSession.getFavoritesDao().delete(bean);
        }
    }

    /**
     *  我喜欢的音乐是否存在
     * */
    public boolean isFavoriteMusic(String mediaID){
        MusicBean bean = getMusic(mediaID, DbConstans.FAVORITES);
        if(null != bean){
            return true;
        }
        return false;
    }


    /*****************************************************************************/

    public List<SearchHis> getSearchHistroy(){
        return searchHisDao.loadAll();
    }

    public void addHistroy(SearchHis his){
        searchHisDao.insertOrReplace(his);
    }

    public void clearAllHistroy(){
        searchHisDao.deleteAll();
    }

}
