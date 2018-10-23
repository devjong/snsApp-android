package com.example.jong.loginactivity1

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.example.jong.loginactivity1.model.ContentDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_detail.view.*
import kotlinx.android.synthetic.main.item_detail.view.*


class DetailViewFragment : Fragment() {

    var firestore: FirebaseFirestore? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        firestore = FirebaseFirestore.getInstance()

        var view = LayoutInflater.from(inflater.context).inflate(R.layout.fragment_detail, container, false)
        view.detailviewfragment_recyclerview.adapter = DetailRecyclerviewAdapter()
        view.detailviewfragment_recyclerview.layoutManager = LinearLayoutManager(activity)
        return view

    }

    inner class DetailRecyclerviewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        val contentDTOs: ArrayList<ContentDTO>?
        val contentUidList: ArrayList<String>

        init {


            contentDTOs = ArrayList()
            contentUidList = ArrayList()

            // 현재 로그인된 유저의 UID(주민등롱번호)
            var uid = FirebaseAuth.getInstance().currentUser?.uid


            // 푸시드리븐 파이어베이스에 방식 데이터가 바뀔 때마다 애가(for) 호출이 된다. 그래서 새로고침이 안에 있어야 리사이클러뷰가 새로고침이 된다.
            firestore?.collection("images")?.orderBy("timestamp")
                ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    contentDTOs.clear()
                    contentUidList.clear()
                    for (snapshot in querySnapshot!!.documents) {
                        var item = snapshot.toObject(ContentDTO::class.java)
                        contentDTOs.add(item)
                        contentUidList.add(snapshot.id)
                    }
                    notifyDataSetChanged()


                }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.item_detail, parent, false)


            return CustomViewHolder(view)
        }

        private inner class CustomViewHolder(view: View?) : RecyclerView.ViewHolder(view)

        override fun getItemCount(): Int {
            return contentDTOs!!.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val viewHolder = (holder as CustomViewHolder).itemView

            // 유저 아이디
            viewHolder.detailviewitem_profile_textview.text = contentDTOs!![position].userId

            // 이미지
            Glide.with(holder.itemView.context).load(contentDTOs!![position].imageUrl)
                .into(viewHolder.detailviewitem_imageview_content)

            //설명 텍스트
            viewHolder.detailviewitem_explain_textview.text = contentDTOs!![position].explain

            // 좋아요 카운터 설정
            viewHolder.detailviewitem_favoritecounter_textview.text = "좋아요 " + contentDTOs!![position].favoriteCount +
                    "개"

            viewHolder.detailviewitem_favorite_imageview.setOnClickListener {
                favoriteEvent(position)
            }

            // 좋아요를 클릭
            var uid = FirebaseAuth.getInstance().currentUser!!.uid
            if (contentDTOs!![position].favorites.containsKey(uid)) {
                viewHolder.detailviewitem_favorite_imageview.setImageResource(R.drawable.ic_favorite)
                // 클릭하지 않았을 경우
            } else {
                viewHolder.detailviewitem_favorite_imageview.setImageResource(R.drawable.ic_favorite_border)
            }

        }

        private fun favoriteEvent(postion: Int) {
            var tsDoc = firestore?.collection("images")?.document(contentUidList[postion])
            firestore?.runTransaction { transaction ->
                var uid = FirebaseAuth.getInstance().currentUser!!.uid
                var contentDTO = transaction.get(tsDoc!!).toObject(ContentDTO::class.java)

                // 좋아요를 누른상태
                if (contentDTO!!.favorites.containsKey(uid)) {
                    contentDTO?.favoriteCount = contentDTO?.favoriteCount - 1
                    contentDTO?.favorites.remove(uid)

                    // 좋아요를 누르지 않은 상태
                } else {
                    contentDTO?.favorites[uid] = true
                    contentDTO?.favoriteCount = contentDTO?.favoriteCount + 1
                }
                transaction.set(tsDoc, contentDTO)

            }
        }
    }

}