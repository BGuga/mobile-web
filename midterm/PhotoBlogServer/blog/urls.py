from django.urls import path, include
from rest_framework import routers
from . import views

router = routers.DefaultRouter()
router.register('post', views.blogImage)

urlpatterns = [
    path('', views.post_list, name='post_list'),  # 작은따옴표를 올바르게 수정
    path('api_root/', include(router.urls)),
]
