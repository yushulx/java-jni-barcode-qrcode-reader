#!/bin/bash

# Bash script to build the complete LiteCam project with native libraries and Java wrapper
# Supports Linux and macOS platforms

set -e  # Exit on any error

# Default parameters
BUILD_DIR="build"
CONFIGURATION="Release"
CLEAN_BUILD=false
SKIP_NATIVE_BUILD=false
VERBOSE=false
EXTRA_NATIVE_DIRS=()

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
GRAY='\033[0;37m'
NC='\033[0m' # No Color

# Emoji support (may not work on all terminals)
if [[ "$TERM" != "dumb" ]] && [[ -t 1 ]]; then
    ROCKET="🚀"
    CHECK="✅"
    ERROR="❌"
    WARNING="⚠️"
    SEARCH="🔍"
    BUILD="🔨"
    JAVA="☕"
    PACKAGE="📦"
    JAR="🏺"
    SCRIPT="📝"
    CLEAN="🧹"
    PARTY="🎉"
    SUMMARY="📊"
    RUN="🚀"
else
    ROCKET="[*]"
    CHECK="[✓]"
    ERROR="[✗]"
    WARNING="[!]"
    SEARCH="[?]"
    BUILD="[#]"
    JAVA="[J]"
    PACKAGE="[P]"
    JAR="[A]"
    SCRIPT="[S]"
    CLEAN="[C]"
    PARTY="[*]"
    SUMMARY="[S]"
    RUN="[R]"
fi

# Helper functions
log_info() {
    echo -e "${GREEN}$1${NC}"
}

log_warn() {
    echo -e "${YELLOW}$1${NC}"
}

log_error() {
    echo -e "${RED}$1${NC}"
}

log_section() {
    echo -e "${CYAN}$1${NC}"
}

log_verbose() {
    if [[ "$VERBOSE" == "true" ]]; then
        echo -e "${GRAY}$1${NC}"
    fi
}

# Function to show usage
show_usage() {
    cat << EOF
Usage: $0 [OPTIONS]

Build the LiteCam Java wrapper with native libraries.

OPTIONS:
    -b, --build-dir DIR       Build directory (default: build)
    -c, --config CONFIG       Build configuration: Release|Debug (default: Release)
    -C, --clean               Clean build (remove build directory first)
    -s, --skip-native         Skip native library build
    -v, --verbose             Enable verbose output
    -e, --extra-natives DIR   Additional directory with prebuilt natives
    -h, --help                Show this help message

EXAMPLES:
    $0                        # Standard build
    $0 --clean --verbose      # Clean build with verbose output
    $0 --skip-native          # Build only Java wrapper (assumes natives exist)
    $0 -e prebuilt-natives    # Include additional prebuilt native libraries

EOF
}

# Parse command line arguments
parse_args() {
    while [[ $# -gt 0 ]]; do
        case $1 in
            -b|--build-dir)
                BUILD_DIR="$2"
                shift 2
                ;;
            -c|--config)
                CONFIGURATION="$2"
                shift 2
                ;;
            -C|--clean)
                CLEAN_BUILD=true
                shift
                ;;
            -s|--skip-native)
                SKIP_NATIVE_BUILD=true
                shift
                ;;
            -v|--verbose)
                VERBOSE=true
                shift
                ;;
            -e|--extra-natives)
                EXTRA_NATIVE_DIRS+=("$2")
                shift 2
                ;;
            -h|--help)
                show_usage
                exit 0
                ;;
            *)
                log_error "$ERROR Unknown option: $1"
                show_usage
                exit 1
                ;;
        esac
    done
}

# Function to check if a command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Function to execute commands with logging
execute_command() {
    local description="$1"
    local command="$2"
    local allow_failure="${3:-false}"
    
    log_warn "$SCRIPT $description..."
    log_verbose "Executing: $command"
    
    if [[ "$VERBOSE" == "true" ]]; then
        if eval "$command"; then
            log_info "$CHECK $description completed successfully"
        else
            local exit_code=$?
            if [[ "$allow_failure" == "true" ]]; then
                log_warn "$WARNING $description failed but continuing..."
            else
                log_error "$ERROR $description failed with exit code $exit_code"
                exit $exit_code
            fi
        fi
    else
        if eval "$command" >/dev/null 2>&1; then
            log_info "$CHECK $description completed successfully"
        else
            local exit_code=$?
            if [[ "$allow_failure" == "true" ]]; then
                log_warn "$WARNING $description failed but continuing..."
            else
                log_error "$ERROR $description failed with exit code $exit_code"
                exit $exit_code
            fi
        fi
    fi
}

# Function to detect platform
get_os_token() {
    local os=$(uname -s | tr '[:upper:]' '[:lower:]')
    case "$os" in
        linux*)
            echo "linux"
            ;;
        darwin*)
            echo "macos"
            ;;
        mingw*|cygwin*|msys*)
            echo "windows"
            ;;
        *)
            echo "unknown"
            ;;
    esac
}

get_arch_token() {
    local arch=$(uname -m)
    case "$arch" in
        x86_64|amd64)
            echo "x86_64"
            ;;
        arm64|aarch64)
            echo "arm64"
            ;;
        *)
            echo "$arch"
            ;;
    esac
}

# Main script
main() {
    parse_args "$@"
    
    local project_root="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
    cd "$project_root"
    
    log_info "$ROCKET Building LiteCam Java Wrapper with Native Libraries..."
    log_section "Project Root: $project_root"
    log_section "Build Directory: $BUILD_DIR"
    log_section "Configuration: $CONFIGURATION"
    
    # Verify required tools
    log_warn "$SEARCH Checking required tools..."
    
    local missing_tools=()
    if ! command_exists cmake; then missing_tools+=("cmake"); fi
    if ! command_exists javac; then missing_tools+=("javac (Java JDK)"); fi
    if ! command_exists jar; then missing_tools+=("jar (Java JDK)"); fi
    
    if [[ ${#missing_tools[@]} -gt 0 ]]; then
        log_error "$ERROR Missing required tools: ${missing_tools[*]}"
        log_error "Please install the missing tools and ensure they are in your PATH."
        exit 1
    fi
    log_info "$CHECK All required tools found"
    
    # Step 1: Build native libraries if not skipped
    if [[ "$SKIP_NATIVE_BUILD" != "true" ]]; then
        log_info "$BUILD Building native libraries..."
        
        local build_path="$project_root/$BUILD_DIR"
        
        if [[ "$CLEAN_BUILD" == "true" ]] && [[ -d "$build_path" ]]; then
            log_warn "$CLEAN Cleaning previous build..."
            rm -rf "$build_path"
        fi
        
        if [[ ! -d "$build_path" ]]; then
            mkdir -p "$build_path"
        fi
        
        pushd "$build_path" > /dev/null
        
        # Configure CMake
        execute_command "CMake configuration" "cmake .."
        
        # Build the project
        execute_command "Native library build" "cmake --build . --config $CONFIGURATION"
        
        # Verify the build output
        local os_token=$(get_os_token)
        local expected_libs=()
        
        case "$os_token" in
            linux)
                expected_libs+=("liblitecam.so")
                ;;
            macos)
                expected_libs+=("liblitecam.dylib")
                ;;
            windows)
                expected_libs+=("$CONFIGURATION/litecam.dll")
                expected_libs+=("litecam.dll")
                ;;
        esac
        
        local found_lib=false
        for lib in "${expected_libs[@]}"; do
            if [[ -f "$lib" ]]; then
                log_info "$CHECK Found native library: $lib"
                found_lib=true
                break
            fi
        done
        
        if [[ "$found_lib" != "true" ]]; then
            log_error "$ERROR No native library found in expected locations"
            log_error "Expected locations: ${expected_libs[*]}"
            popd > /dev/null
            exit 1
        fi
        
        popd > /dev/null
    else
        log_warn "⏭️ Skipping native build (skip-native flag set)"
    fi
    
    # Step 2: Build Java wrapper
    log_info "$JAVA Building Java wrapper..."
    
    local java_src="$project_root/java-src"
    local out_dir="$project_root/java-build"
    
    if [[ -d "$out_dir" ]]; then
        log_warn "$CLEAN Cleaning previous Java build..."
        rm -rf "$out_dir"
    fi
    mkdir -p "$out_dir"
    
    log_warn "$SCRIPT Compiling Java sources..."
    
    local java_files=($(find "$java_src" -name "*.java" -type f))
    log_verbose "Java files found: ${java_files[*]}"
    
    execute_command "Java compilation" "javac -d \"$out_dir\" ${java_files[*]}"
    
    local jar_path="$project_root/litecam.jar"
    if [[ -f "$jar_path" ]]; then
        log_warn "🗑️ Removing existing JAR..."
        rm "$jar_path"
    fi
    
    # Step 3: Package native libraries
    log_info "$PACKAGE Packaging native libraries..."
    
    local os_token=$(get_os_token)
    local arch_token=$(get_arch_token)
    
    log_section "Detected platform: $os_token-$arch_token"
    
    # Collect native libraries by platform/arch
    local dll_candidates=()
    dll_candidates+=("$project_root/build/$CONFIGURATION/litecam.dll")
    dll_candidates+=("$project_root/build/litecam.dll")
    dll_candidates+=("$project_root/build/$CONFIGURATION/liblitecam.dylib")
    dll_candidates+=("$project_root/build/liblitecam.dylib")
    dll_candidates+=("$project_root/build/$CONFIGURATION/liblitecam.so")
    dll_candidates+=("$project_root/build/liblitecam.so")
    
    # Primary host build native bundling
    local native_root="$out_dir/natives"
    mkdir -p "$native_root"
    local native_dir="$native_root/$os_token-$arch_token"
    mkdir -p "$native_dir"
    
    local picked=false
    for candidate in "${dll_candidates[@]}"; do
        if [[ -f "$candidate" ]]; then
            case "$os_token" in
                windows)
                    dest_name="litecam.dll"
                    ;;
                macos)
                    dest_name="liblitecam.dylib"
                    ;;
                *)
                    dest_name="liblitecam.so"
                    ;;
            esac
            
            cp "$candidate" "$native_dir/$dest_name"
            log_info "$PACKAGE Bundling native library: $candidate -> natives/$os_token-$arch_token/$dest_name"
            picked=true
            break
        fi
    done
    
    if [[ "$picked" != "true" ]]; then
        log_warn "$WARNING No native library found to bundle for host platform ($os_token-$arch_token)"
        log_warn "Searched locations:"
        for candidate in "${dll_candidates[@]}"; do
            log_verbose "  - $candidate"
        done
    fi
    
    # Merge additional prebuilt native sets
    if [[ ${#EXTRA_NATIVE_DIRS[@]} -gt 0 ]]; then
        log_info "🔗 Merging additional prebuilt natives..."
        for dir in "${EXTRA_NATIVE_DIRS[@]}"; do
            if [[ ! -d "$dir" ]]; then
                log_warn "$WARNING Extra native dir not found: $dir"
                continue
            fi
            
            # Expect structure like <dir>/linux-x86_64/liblitecam.so etc.
            for platform_dir in "$dir"/*; do
                if [[ -d "$platform_dir" ]]; then
                    local platform_name=$(basename "$platform_dir")
                    local target="$native_root/$platform_name"
                    mkdir -p "$target"
                    
                    find "$platform_dir" -type f -exec cp {} "$target/" \;
                    log_info "$PACKAGE Merged extra natives: $platform_name"
                fi
            done
        done
    fi
    
    # Step 4: Create JAR with manifest
    log_info "$JAR Creating JAR with bundled natives..."
    
    # Create a proper manifest
    local manifest_dir="$out_dir/META-INF"
    mkdir -p "$manifest_dir"
    local manifest_file="$manifest_dir/MANIFEST.MF"
    
    cat > "$manifest_file" << EOF
Manifest-Version: 1.0
Main-Class: com.example.litecam.LiteCamViewer
Implementation-Title: LiteCam Java Wrapper
Implementation-Version: 1.0.0
Implementation-Vendor: LiteCam Project
Built-Date: $(date '+%Y-%m-%d %H:%M:%S')
Built-Platform: $os_token-$arch_token

EOF
    
    pushd "$out_dir" > /dev/null
    execute_command "JAR creation" "jar cfm \"$jar_path\" META-INF/MANIFEST.MF ."
    popd > /dev/null
    
    # Step 5: Verify JAR contents
    log_info "$SEARCH Verifying JAR contents..."
    local jar_contents=$(jar -tf "$jar_path")
    local expected_files=(
        "com/example/litecam/LiteCam.class"
        "com/example/litecam/LiteCamViewer.class"
        "META-INF/MANIFEST.MF"
    )
    
    local missing_files=()
    for expected in "${expected_files[@]}"; do
        if ! echo "$jar_contents" | grep -q "^$expected$"; then
            missing_files+=("$expected")
        fi
    done
    
    if [[ ${#missing_files[@]} -gt 0 ]]; then
        log_warn "$WARNING Missing expected files in JAR: ${missing_files[*]}"
    else
        log_info "$CHECK All expected files found in JAR"
    fi
    
    # Count native libraries
    local native_files=($(echo "$jar_contents" | grep '^natives/.*[^/]$' || true))
    log_section "$PACKAGE Native libraries included: ${#native_files[@]}"
    for native_file in "${native_files[@]}"; do
        log_verbose "  - $native_file"
    done
    
    log_info "$CHECK Created $jar_path"
    
    # Step 6: Create convenience scripts
    log_info "$SCRIPT Creating convenience scripts..."
    
    # Bash run script
    cat > "$project_root/run-litecam.sh" << 'EOF'
#!/bin/bash

# Script to run LiteCam Java Viewer

set -e

script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
jar_path="$script_dir/litecam.jar"

if [[ ! -f "$jar_path" ]]; then
    echo "Error: litecam.jar not found in $script_dir" >&2
    echo "Please run build-jar.sh first to build the JAR file." >&2
    exit 1
fi

echo "Starting LiteCam Java Viewer..."
java -cp "$jar_path" com.example.litecam.LiteCamViewer "$@"
EOF
    
    chmod +x "$project_root/run-litecam.sh"
    log_info "$CHECK Created run-litecam.sh"
    
    # Step 7: Final summary
    echo ""
    log_info "$PARTY Build completed successfully!"
    log_section "$SUMMARY Summary:"
    echo -e "  JAR file: ${jar_path}"
    echo -e "  File size: $(du -h "$jar_path" | cut -f1)"
    echo -e "  Native libraries: ${#native_files[@]}"
    echo -e "  Target platform: $os_token-$arch_token"
    echo ""
    log_warn "$RUN To run the application:"
    echo -e "  Bash: ./run-litecam.sh"
    echo -e "  Manual: java -cp litecam.jar com.example.litecam.LiteCamViewer"
}

# Run main function
main "$@"
