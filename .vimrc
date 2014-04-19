":cd ~/

" ■文字コード設定
if has('gui_running') && !has('unix')
  set encoding=utf-8
endif
set fileencodings=ucs-bom,iso-2022-jp,utf-8,cp932,euc-jp,default,latin
set termencoding=utf-8

" ■タブ、改行
set tabstop=4
set formatoptions=q

" ■ Windows gitのdiff設定
let $TERM='msys'

" ■perl-support
let g:Perl_Perltidy  = 'on'
let g:Perl_CodeSnippets  = $HOME . '/vim/bundles/perl-support.vim/perl-support/codesnippets/'

" コマンド履歴の設定
set viminfo='50,<2000,s100,:0,

" 開いたファイルのパスをカレントにする
"set autochdir

" ■バックアップを/tmpにとる
set backupdir=/tmp

" ■ステータスラインにファイルエンコーディングと日時を表示する
function! GetStatusEx()
  let str = ''
  if &ft != ''
    let str = str . '[' . &ft . ']'
  endif
  if has('multi_byte')
    if &fenc != ''
      let str = str . '[' . &fenc . ']'
    elseif &enc != ''
      let str = str . '[' . &enc . ']'
    endif
  endif
  if &ff != ''
    let str = str . '[' . &ff . ']'
  endif
  return str
endfunction
function! g:Date()
    return strftime("%x(%a) %H:%M")
endfunction

" ■ステータスライン
set statusline=%<%f\ %m%r%h%w%=\ %{fugitive#statusline()}\ %{g:Date()}\ \%{GetStatusEx()}\ \ %l,%c%V%8P


" ■インサートモード時のハイライト
let g:hi_insert = 'highlight StatusLine guifg=darkblue guibg=darkyellow gui=none ctermfg=blue ctermbg=yellow cterm=none'

if has('syntax') 
  augroup InsertHook    
    autocmd!
    autocmd InsertEnter * call s:StatusLine('Enter')
    autocmd InsertLeave * call s:StatusLine('Leave')  
  augroup END
endif

let s:slhlcmd = ''
function! s:StatusLine(mode)  
  if a:mode == 'Enter'
	  silent! let s:slhlcmd = 'highlight ' . s:GetHighlight('StatusLine')    
	  silent exec g:hi_insert  
  else    
	  highlight clear StatusLine    
	  silent exec s:slhlcmd  
  endif
endfunction

function! s:GetHighlight(hi)  
	redir => hl  
	exec 'highlight '.a:hi  
	redir END  
	let hl = substitute(hl, '[\r\n]', '', 'g')  
	let hl = substitute(hl, 'xxx', '', '')  
	return hl
endfunction

" ■unite
nnoremap    [unite]   <Nop>
nmap    <Leader>f [unite]
nnoremap [unite]u  :<C-u>Unite -no-split<Space>
nnoremap <silent> [unite]f :<C-u>Unite<Space>buffer<CR>
nnoremap <silent> [unite]b :<C-u>Unite<Space>bookmark<CR>
nnoremap <silent> [unite]m :<C-u>Unite<Space>file_mru<CR>
nnoremap <silent> [unite]r :<C-u>UniteWithBufferDir file<CR>
nnoremap <silent> ,vr :UniteResume<CR>

" ■Git
nnoremap [git]     <Nop>
nmap     <Leader>g [git]
nnoremap <silent>  [git]c :Gwrite<CR>:Gcommit -m 'hoge'<CR>:Git push<CR>
nnoremap <silent>  [git]r :Git pull origin master<CR>
nnoremap <silent>  [git]s :Gstatus<CR>

" ■ タブ操作
map <C-TAB> :tabnext<CR>

" ■SrcExpl
"自動でプレビューを表示する。
let g:SrcExpl_RefreshTime   = 1
"プレビューウインドウの高さ
let g:SrcExpl_WinHeight     = 9
"tagsは自動で作成する
let g:SrcExpl_UpdateTags    = 1
"マッピング
let g:SrcExpl_RefreshMapKey = "<Space>"
let g:SrcExpl_GoBackMapKey  = "<C-b>"
nmap <F8> ::TrinityToggleAll<CR>
nmap <F7> ::TrinityToggleSourceExplorer<CR>

" ■ 画面でかくする
if has('win32') 
    nmap <Leader>s :SM 4<CR>
endif

" ■ 開いてるファイルのディレクトリをカレントディレクトリ
nmap <Leader>cd :cd %:p:h<CR>

"map <C-w> :tabclose<CR>
map <C-n> :tabnew<CR>

" ■インクリメンタルサーチ
set incsearch

" ■ag
nnoremap <F3> :Ag <C-R><C-W><CR>

" ■w3m
let g:w3m#external_browser = '"c:\Program Files\Internet Explorer\iexplore.exe"'
nnoremap [w3m]     <Nop>
nmap     <Leader>w [w3m]
nnoremap [w3m]i :W3mShowExtenalBrowser<CR>
nnoremap [w3m]r :W3mReload<CR>
nnoremap [w3m]h :W3mHistory<CR>
nnoremap [w3m]w :call ChgProxy(0)<CR>:W3m http://www.google.co.jp/search?as_q=<C-R><C-W><CR>
nnoremap [w3m]k :call ChgProxy(0)<CR>:W3m http://info.finance.yahoo.co.jp/fx/list/<CR>
nnoremap [w3m]s :call ChgProxy(0)<CR>:<C-u>:W3m http://www.google.co.jp/search?as_q=
nnoremap [w3m]m :call ChgProxy(0)<CR>:W3m http://yoshi1108.github.com/yosh/memo.html<CR>
nnoremap [w3m]2 :call ChgProxy(1)<CR>:W3m http://www.2nn.jp/<CR>
nnoremap [w3m]ik :call ChgProxy(1)<CR>:W3m http://www.invest-keiba.com/articles/top/index.html?mid=32bbae6495da0cfb7e05f0b810fd1e2e&no=7333<CR>
nnoremap [w3m]da :call ChgProxy(1)<CR>:W3m http://www.dabiana.com/articles/top/2/index.html?usid=c90bc7a7c8fa65b293597b0752f75759&no=7984<CR>
nnoremap [w3m]ni :call ChgProxy(1)<CR>:W3m http://sp.ch.nicovideo.jp/portal/anime<CR>
nnoremap [w3m]p :call ChgProxy('')<CR>

" プロクシの切り替え。指定なければ交互に切り替え
let s:http_proxy_mode='0'
function! ChgProxy(mode)
    if ( a:mode != '' )
        let s:http_proxy_mode=a:mode
	endif
    if ( s:http_proxy_mode == 0 )
        let $HTTP_PROXY='http://proxygate1.nic.nec.co.jp:8080'
        "let $HTTP_PROXY=''
        let s:http_proxy_mode='1'
    elseif ( s:http_proxy_mode == 1 )
        let $HTTP_PROXY='http://localhost:8888'
        "let $HTTP_PROXY='http://192.168.1.3:8080'
        let s:http_proxy_mode='0'
    endif
	echo $HTTP_PROXY
endfunction

" ■Webhome
source ~/webhome.vim
nmap <F12> :Webhome<CR>

" ■ Ctrl-spaceで補完
if has('unix') 
    inoremap <Nul> <C-n>
elseif has('win32')
    inoremap <C-Space> <C-n>
endif

" ■ vimrcのショートカット
nnoremap <Space>. :edit $MYVIMRC<CR>
nnoremap <Space>s. :source $MYVIMRC<CR>

" ■ perltidy
if has('unix')
    map <Space>p ! perltidy<CR>
elseif has('win32')
    map <Space>p ! perl /usr/bin/perltidy.pl -pro=/oracle/home/.perltidyrc -st<CR>
endif

" ■ neosnippet
" Plugin key-mappings.
imap <C-k>     <Plug>(neosnippet_expand_or_jump)
smap <C-k>     <Plug>(neosnippet_expand_or_jump)
xmap <C-k>     <Plug>(neosnippet_expand_target)

" SuperTab like snippets behavior.
imap <expr><TAB> neosnippet#expandable_or_jumpable() ?
\ "\<Plug>(neosnippet_expand_or_jump)"
\: pumvisible() ? "\<C-n>" : "\<TAB>"
smap <expr><TAB> neosnippet#expandable_or_jumpable() ?
\ "\<Plug>(neosnippet_expand_or_jump)"
\: "\<TAB>"

" For snippet_complete marker.
if has('conceal')
  set conceallevel=2 concealcursor=i
endif

" ■ neobundle
set nocompatible
if has('unix')
	if has('vim_starting')
		set runtimepath+=~/vim/plugins/neobundle.vim-master
    endif
elseif has('win32')
    if has('vim_starting')
    	set runtimepath+=~/vim/plugins/neobundle.vim-master/plugin/neobundle.vim
    endif
endif


call neobundle#rc(expand('~/vim/bundles/'))
NeoBundle 'Shougo/vimshell'
if has('unix')
	NeoBundle 'Shougo/vimproc'
endif
NeoBundle 'Shougo/unite.vim'
NeoBundle 'Shougo/neocomplcache'
NeoBundle 'Shougo/neosnippet'
NeoBundle 'Shougo/neosnippet-snippets'
NeoBundle 'Shougo/neomru.vim'
NeoBundle 'thinca/vim-ref'
NeoBundle 'thinca/vim-quickrun'
NeoBundle 'thinca/vim-singleton'
NeoBundle 'itchyny/calendar.vim'
NeoBundle 'tyru/open-browser.vim'
NeoBundle 'mattn/webapi-vim'
NeoBundle 'mhinz/vim-startify'
NeoBundle 'groovy.vim'
NeoBundle 'tpope/vim-fugitive'
NeoBundle 'vim-scripts/L9'
"NeoBundle 'vim-scripts/AutoComplPop'
"NeoBundle 'vim-scripts/ctags.vim' " uniteとぶつかる気がする
NeoBundle 'vim-scripts/SrcExpl'
NeoBundle 'vim-scripts/taglist.vim'
NeoBundle 'vim-scripts/Trinity'
"iNeoBundle 'vim-scripts/SingleCompile'
NeoBundle 'vim-jp/vital.vim'
NeoBundle 'kmnk/vim-unite-svn'
NeoBundle 'rking/ag.vim'
NeoBundle 'perl-support.vim'
NeoBundle 'derekwyatt/vim-scala'
NeoBundle 'yuratomo/w3m.vim'
NeoBundle 'yuratomo/gmail.vim'
filetype plugin indent on

" ■gmail
source ~/.gmailrc
nmap <F2> :call ChgProxy(1)<CR>:Gmail<CR>

" ■quickRun
let g:quickrun_config = {
\   "hook/output_encode/enable" : 1,
\   "hook/output_encode/encoding" : "sjis",
\   "_" : {
\       "outputter" : "multi:buffer:quickfix",
\       "runner" : "vimproc",
\       "runner/vimproc/updatetime" : 40,
\   },
\
\   "java" : {
\       'exec' : ['javac -J-Dfile.encoding=UTF8 %o %s', '%c -Dfile.encoding=UTF8 %s:t:r %a']
\   },
\}



" \       "outputter/buffer/split" : ":botright 8sp",

" groovy設定
let g:quickrun_config.groovy = {'command' : 'groovy', 'cmdopt' : ''}

"let g:quickrun_config.scala = {'cmdopt' : '-Dfile.encoding=' . '&termencoding' , 'hook/output_encode/encoding' : '&termencoding'}
nnoremap <expr><silent> <C-c> quickrun#is_running() ? quickrun#sweep_sessions() : "\<C-c>"

" ■ 選択箇所のコピー、右クリックでペースト
" copy paste GUI
set guioptions+=a
" copy paste CUI
set clipboard+=autoselect

nnoremap <RightMouse> "*p
inoremap <RightMouse> <C-r><C-o>

" ■ startify 
let s:memofile = $HOME . "/memo.txt"

" メモファイルを開く
nnoremap <Space>m :edit $HOME/memo.txt<CR>

let s:memo_list = []
for line in readfile(s:memofile)
	call add(s:memo_list, line)
endfor

let g:startify_custom_header = map([g:Date()], '"   ". v:val')
let g:startify_custom_footer = map(s:memo_list, '"   ". v:val')

let g:startify_bookmarks = [
  \ '~/.vimrc',
  \ 'c:\oracle',
  \ 'y:\',
  \ ]

" ■calender-vim設定
let g:calendar_google_calendar = 1
let g:calendar_google_task = 1

" ■ vim常駐化
if has('win32') 
    call singleton#enable()
endif

