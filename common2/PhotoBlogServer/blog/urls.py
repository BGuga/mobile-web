from django.urls import path, include
from rest_framework import routers
from . import views
from django.conf import settings
from django.conf.urls.static import static

router = routers.DefaultRouter()
router.register('post', views.blogImage)
router.register('posts', views.BlogImageViewSet, "blogimage")

urlpatterns = [
    path('', views.post_list, name='post_list'),
    path('post/<int:pk>/', views.post_detail, name='post_detail'),
    path('post/new/', views.post_new, name='post_new'),
    path('post/<int:pk>/edit/', views.post_edit, name='post_edit'),
    path('api_root/', include(router.urls)),
    path('api_root/search/<str:currentStatus>/', views.FilteredPostsView.as_view()),
] + static(settings.MEDIA_URL, document_root=settings.MEDIA_ROOT)

