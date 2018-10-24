package com.example.jong.loginactivity1

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.jong.loginactivity1.model.ContentDTO
import com.example.jong.loginactivity1.model.FollowDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_user.*
import kotlinx.android.synthetic.main.fragment_user.view.*

class UserFragment : Fragment() {
    var fragmentView: View? = null
    var PICK_PROFILE_FROM_ALBUM = 10
    var firestore: FirebaseFirestore? = null

    // 현재 나이 uid
    var currentUserUid: String? = null

    // 내가 선택한 uid
    var uid: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
//        uid = currentUserUid
        if (arguments != null) {
            uid = arguments!!.getString("destinationUid")
        } else {
            uid = currentUserUid
        }





        firestore = FirebaseFirestore.getInstance()
        fragmentView = LayoutInflater.from(inflater.context).inflate(R.layout.fragment_user, container, false)

        // Profile Image Click Listener
        fragmentView?.account_id_profile?.setOnClickListener {
            var photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            activity?.startActivityForResult(photoPickerIntent, PICK_PROFILE_FROM_ALBUM)


        }
        fragmentView?.accountrecyclerview?.adapter = UseFragmentRecyclerViewAdapter()
        fragmentView?.accountrecyclerview?.layoutManager = GridLayoutManager(activity!!, 3)
        getProfileImages()

        fragmentView?.account_btn_follow_signout?.setOnClickListener {
            requestFollow()
        }
        return fragmentView
    }

    fun requestFollow() {
        var tsDocFollowing = firestore!!.collection("users").document(currentUserUid!!)

        firestore?.runTransaction { transaction ->
            var followDTO = transaction.get(tsDocFollowing).toObject(FollowDTO::class.java)

            // 아무도 팔로잉 하지 않았을 경우
            if (followDTO == null) {
                followDTO = FollowDTO()
                followDTO.followingCount = 1
                followDTO!!.followings[uid!!] = true

                transaction.set(tsDocFollowing, followDTO)
                return@runTransaction
            }

            // 내 아이디가 제3자를 이미 팔로잉 하고 있을 경우 ->제 3자가 나를 팔로워 취소한다.
            if (followDTO.followings.containsKey(uid)) {
                followDTO?.followCount = followDTO?.followCount - 1
                followDTO?.followings.remove(uid)
            } else {
            // 내가 제 3자를 팔로잉 하지 않았을 경우(제4자가 3자를 팔로잉 하고 있음) -> 제3자가 나를 팔로워 한다.
                followDTO.followingCount = followDTO.followCount + 1
                followDTO.followings[uid!!] = true

            }

            transaction.set(tsDocFollowing, followDTO)
            return@runTransaction
        }

        var tsDocFollower = firestore!!.collection("users").document(uid!!)
        firestore?.runTransaction { transaction ->
            var followDTO = transaction.get(tsDocFollower).toObject(FollowDTO::class.java)

            if (followDTO == null) {
                // 아무도 팔로워 하지 않았을 경우
                followDTO = FollowDTO()
                followDTO!!.followCount = 1
                followDTO!!.followers[currentUserUid!!] = true

                transaction.set(tsDocFollower, followDTO)
                return@runTransaction
            }

            // 제3자의 유저를 내가 팔로잉 하고 있을 때
            if(followDTO.followers.containsKey(currentUserUid!!)){

                followDTO!!.followCount = followDTO!!.followCount - 1
                followDTO!!.followers.remove(currentUserUid!!)

                transaction.set(tsDocFollower, followDTO)
            } else {
                // 제 3자를 내가 팔로워 하지 않았을 경우 -> 팔로워 하겠따.

                followDTO.followCount = followDTO.followCount + 1
                followDTO.followers[currentUserUid!!] = true
            }
            transaction.set(tsDocFollower, followDTO)
            return@runTransaction
        }

    }


    fun getProfileImages() {
        firestore?.collection("profileImages")?.document(uid!!)
            ?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                if (documentSnapshot.data != null) {
                    var url = documentSnapshot?.data!!["image"]
                    Glide.with(activity).load(url).apply(RequestOptions().circleCrop())
                        .into(fragmentView!!.account_id_profile)

                }
            }

    }


    inner class UseFragmentRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        var contentDTOs: ArrayList<ContentDTO> = ArrayList()

        init {
            firestore?.collection("images")?.whereEqualTo("currentUid", currentUserUid)
                ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->

                    for (snapshot in querySnapshot.documents) {
                        contentDTOs.add(snapshot.toObject(ContentDTO::class.java))
                    }
                    account_id_post_count.text = contentDTOs.size.toString()
                    notifyDataSetChanged()

                }
        }


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var width = resources.displayMetrics.widthPixels / 3

            var imageView = ImageView(parent.context)
            imageView.layoutParams = LinearLayoutCompat.LayoutParams(width, width)
            return CustomViewHolder(imageView)
        }

        inner class CustomViewHolder(var imageView: ImageView) : RecyclerView.ViewHolder(imageView)

        override fun getItemCount(): Int {
            return contentDTOs.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var imageView = (holder as CustomViewHolder).imageView
            Glide.with(holder.itemView.context).load(contentDTOs[position].imageUrl)
                .apply(RequestOptions().centerCrop()).into(imageView)
        }
    }


}

