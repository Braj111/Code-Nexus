package com.example.musicplayer

import android.Manifest
import android.annotation.SuppressLint
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.Toast
import android.widget.Toolbar
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import java.io.File

open class MainActivity : AppCompatActivity() {
    companion object{
        var songList:ArrayList<Song> = arrayListOf()
    }

    val internal_Sorage:ActivityResultLauncher<String> = registerForActivityResult(ActivityResultContracts.RequestPermission()){
        isGranted->
        if(isGranted){
            Toast.makeText(this,"Internal storage access granted",Toast.LENGTH_SHORT).show()
            songList.addAll(getAllAudio())
        }
        else{
            Toast.makeText(this,"Internal storage access denied",Toast.LENGTH_SHORT).show()
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNavigationView:BottomNavigationView=findViewById(R.id.bottomNavView)
        val toolbar:androidx.appcompat.widget.Toolbar=findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        launchPermission()

        val recyclerView:RecyclerView=findViewById(R.id.recyclerView)

        val myAdapter:MyAdapter= MyAdapter(this,songList)
        recyclerView.adapter=myAdapter
        recyclerView.layoutManager=LinearLayoutManager(this)

        bottomNavigationView.setOnItemSelectedListener { item:MenuItem->Boolean
            when(item.itemId){
                R.id.AllSongs->{
                    Toast.makeText(this,"All Songs Clicked",Toast.LENGTH_SHORT).show()
                }
                R.id.playlists->{
                    Toast.makeText(this,"Playlists Clicked",Toast.LENGTH_SHORT).show()
                }
                R.id.favourites->{
                    Toast.makeText(this,"Favourites Clicked",Toast.LENGTH_SHORT).show()
                }
            }
            true
        }
    }



    @SuppressLint("Range")
    fun getAllAudio():ArrayList<Song> {
        var musicList:ArrayList<Song> = arrayListOf()
        val selection=MediaStore.Audio.Media.IS_MUSIC + " !=0"
        val projection= arrayOf(MediaStore.Audio.Media._ID,MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID)

        val cursor=this.contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,projection,selection,null,
            null,null)


        if(cursor!=null){

            if(cursor.moveToFirst()) {


                do {
                    val titleC =
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
                    val idC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID))
                    val artistC =
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))
                    val albumC =
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM))
                    val pathC =
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))
                    val durationC =
                        cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))
                    val albumIdC=
                        cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)).toString()
                    val uri: Uri =Uri.parse("content://media/external/audio/albumart")
                    val artUriC=Uri.withAppendedPath(uri,albumIdC).toString()
                    val song = Song(
                        title = titleC,
                        artist = artistC,
                        id = idC,
                        album = albumC,
                        path = pathC,
                        duration = durationC,
                        artUri = artUriC)
                    val file = File(song.path)
                    if (file.exists()) {
                        musicList.add(song)
                    }
                } while (cursor.moveToNext())
                cursor.close()
            }

        }

        return musicList

    }


    private fun launchPermission() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M &&
            shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        ) {
            showAlertDialog("Can't access music files", "You have denied storage access")
        } else {
            internal_Sorage.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    private fun showAlertDialog(title: String, message: String) {
        val build: AlertDialog.Builder=AlertDialog.Builder(this)
        build.setTitle(title)
        build.setMessage(message)
        build.setPositiveButton("Cancel"){dialog,_->
            dialog.dismiss()
        }
        build.create().show()
    }
}