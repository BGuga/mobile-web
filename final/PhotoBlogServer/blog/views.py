from django.shortcuts import render, get_object_or_404
from django.utils import timezone
from .models import Post
from .forms import PostForm
from rest_framework.response import Response
from django.shortcuts import redirect
from rest_framework import viewsets
from .serializers import PostSerializer, JsonSerializer
from rest_framework.views import APIView

def post_list(request):
    posts = Post.objects.filter(published_date__lte=timezone.now()).order_by('published_date')
    return render(request, 'blog/post_list.html', {'posts': posts})

def post_detail(request, pk):
    post = get_object_or_404(Post, pk=pk)
    return render(request, 'blog/post_detail.html', {'post': post})

def post_new(request):
    if request.method == "POST":
        form = PostForm(request.POST)
        if form.is_valid():
            post = form.save(commit=False)
            post.author = request.user
            post.published_date = timezone.now()
            post.save()
            return redirect('post_detail', pk=post.pk)
    else:
        form = PostForm()
    return render(request, 'blog/post_edit.html', {'form': form})

def post_edit(request, pk):
    post = get_object_or_404(Post, pk=pk)
    if request.method == "POST":
        form = PostForm(request.POST, instance=post)
        if form.is_valid():
            post = form.save(commit=False)
            post.author = request.user
            post.published_date = timezone.now()
            post.save()
            return redirect('post_detail', pk=post.pk)
    else:
        form = PostForm(instance=post)
    return render(request, 'blog/post_edit.html', {'form': form})

class blogImage(viewsets.ModelViewSet):
    queryset = Post.objects.all()
    serializer_class = PostSerializer


class BlogImageViewSet(viewsets.ReadOnlyModelViewSet):
    queryset = Post.objects.all()
    serializer_class = JsonSerializer


class FilteredPostsView(APIView):
    def get(self, request, currentStatus):
        print("called")  
        queryset = Post.objects.filter(text__icontains=currentStatus)
        serializer = PostSerializer(queryset, many=True)
        return Response(serializer.data)

class RecentPostView(APIView):
    def get(self, request):
        # 가장 최근의 글 하나 가져오기
        recent_post = Post.objects.filter(published_date__lte=timezone.now()).order_by('-published_date').first()
        if recent_post:
            serializer = PostSerializer(recent_post)
            return Response(serializer.data, status=200)
        else:
            return Response({"detail": "No posts found."}, status=404)
